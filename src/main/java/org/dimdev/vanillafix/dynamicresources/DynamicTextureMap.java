package org.dimdev.vanillafix.dynamicresources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelDynBucket;
import net.minecraftforge.client.model.ModelLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DynamicTextureMap extends TextureMap {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final IResourceManager resourceManager = minecraft.getResourceManager();
    private final Map<String, TextureAtlasSprite> loadedSprites = new ConcurrentHashMap<>();
    private final List<TextureAtlasSprite> spritesNeedingUpload = new CopyOnWriteArrayList<>();
    private final Lock spriteLoadingLock = new ReentrantLock();
    private DynamicStitcher stitcher = null;
    private boolean atlasNeedsExpansion = false;

    public DynamicTextureMap(String basePath) {
        super(basePath);
    }

    @Override
    public void loadTexture(IResourceManager resourceManager) {}

    @Override
    public void loadSprites(IResourceManager resourceManager, ITextureMapPopulator iconCreatorIn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadTextureAtlas(IResourceManager resourceManager) {
        throw new UnsupportedOperationException();
    }

    public void init() {
        deleteGlTexture();
        int maximumTextureSize = Minecraft.getGLMaximumTextureSize();

        stitcher = new DynamicStitcher(maximumTextureSize, maximumTextureSize, 0, mipmapLevels);
        listAnimatedSprites.clear();

        initMissingImage();
        missingImage.generateMipmaps(mipmapLevels);
        spritesNeedingUpload.add(missingImage);
        stitcher.addSprite(missingImage);

        TextureUtil.allocateTextureImpl(getGlTextureId(), mipmapLevels, stitcher.getImageWidth(), stitcher.getImageHeight());
        LOGGER.info("Created {}x{} '{}' atlas", stitcher.getImageWidth(), stitcher.getImageHeight(), basePath);

        EventUtil.postEventAllowingErrors(new TextureStitchEvent.Pre(this));
        ModelLoader.White.INSTANCE.register(this);
        mapRegisteredSprites.put("builtin/white", ModelLoader.White.INSTANCE); // TODO: why is this necessary
        ModelDynBucket.LoaderDynBucket.INSTANCE.register(this);

        EventUtil.postEventAllowingErrors(new TextureStitchEvent.Post(this));
    }

    @Override
    public TextureAtlasSprite getAtlasSprite(String iconName) {
        TextureAtlasSprite sprite = loadedSprites.get(iconName);
        if (sprite == null) {
            spriteLoadingLock.lock();
            try {
                sprite = loadedSprites.get(iconName);
                if (sprite == null) {
                    sprite = loadSprite(iconName);
                    loadedSprites.put(iconName, sprite);
                }
            } finally {
                spriteLoadingLock.unlock();
            }
        }
        return sprite;
    }

    protected TextureAtlasSprite loadSprite(String name) {
//        LOGGER.info("Loading texture " + name);

        TextureAtlasSprite sprite = mapRegisteredSprites.get(name);
        if (sprite == null) {
            sprite = new TextureAtlasSprite(name);
        }

        // Load the sprite
        ResourceLocation location = getResourceLocation(sprite);

        if (sprite.hasCustomLoader(resourceManager, location)) {
            sprite.load(resourceManager, location, l -> getAtlasSprite(l.toString()));
        } else if (name.equals("minecraft:missingno")) {
            return missingImage;
        } else {
            try (IResource resource = resourceManager.getResource(location)) {
                PngSizeInfo pngSizeInfo = PngSizeInfo.makeFromResource(resourceManager.getResource(location));
                boolean isAnimated = resource.getMetadata("animation") != null;
                sprite.loadSprite(pngSizeInfo, isAnimated);
            } catch (Throwable t) {
                LOGGER.error("Couldn't load sprite " + location, t);
                return missingImage;
            }
        }

        if (!generateMipmaps(resourceManager, sprite)) {
            return missingImage;
        }

        // Allocate it a spot on the texture map
        int oldWidth = stitcher.getImageWidth();
        int oldHeight = stitcher.getImageHeight();

        stitcher.addSprite(sprite);

        // Texture map got resized, recreate it and upload all textures
        if (stitcher.getImageWidth() != oldWidth || stitcher.getImageHeight() != oldHeight) {
            atlasNeedsExpansion = true;
        }

        // Upload the texture
        spritesNeedingUpload.add(sprite);

        // Add animated sprites to a list so that it's ticked every tick
        if (sprite.hasAnimationMetadata()) {
            listAnimatedSprites.add(sprite);
        }

        // Updade if calling from Minecraft thread so that inventory items don't flicker
        // Don't load during init to avoid problems with loading screen
        if (minecraft.isCallingFromMinecraftThread() && ((IPatchedMinecraft) minecraft).isDoneLoading()) {
            update();
        }

        return sprite;
    }

    public void update() {
        minecraft.profiler.startSection("updateTextureMap");

        if (atlasNeedsExpansion) {
            atlasNeedsExpansion = false;

            minecraft.profiler.startSection("expandAtlas");

            int newWidth = stitcher.getImageWidth();
            int newHeight = stitcher.getImageHeight();
            LOGGER.info("Expanding '{}' atlas to {}x{}", basePath, newWidth, newHeight);

            TextureScaleInfo.textureId = -1;
            TextureUtil.allocateTextureImpl(getGlTextureId(), mipmapLevels, newWidth, newHeight);

            TextureScaleInfo.textureId = getGlTextureId();
            TextureScaleInfo.xScale = (double) DynamicStitcher.BASE_WIDTH / newWidth;
            TextureScaleInfo.yScale = (double) DynamicStitcher.BASE_HEIGHT / newHeight;

            GlStateManager.bindTexture(getGlTextureId());
            for (TextureAtlasSprite loadedSprite : stitcher.getAllSprites()) {
                TextureUtil.uploadTextureMipmap(loadedSprite.getFrameTextureData(0), loadedSprite.getIconWidth(), loadedSprite.getIconHeight(), loadedSprite.getOriginX(), loadedSprite.getOriginY(), false, false);
            }

            minecraft.profiler.endSection();
        }

        minecraft.profiler.startSection("uploadTexture");
        GlStateManager.bindTexture(getGlTextureId());
        for (TextureAtlasSprite sprite : spritesNeedingUpload) {
            spritesNeedingUpload.remove(sprite);
            TextureUtil.uploadTextureMipmap(sprite.getFrameTextureData(0), sprite.getIconWidth(), sprite.getIconHeight(), sprite.getOriginX(), sprite.getOriginY(), false, false);
        }
        minecraft.profiler.endSection();

        minecraft.profiler.endSection();
    }
}
