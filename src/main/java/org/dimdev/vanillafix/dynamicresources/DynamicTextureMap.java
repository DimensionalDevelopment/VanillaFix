package org.dimdev.vanillafix.dynamicresources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
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
    protected DynamicStitcher stitcher;
    protected IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
    private Map<String, TextureAtlasSprite> loadedSprites = new ConcurrentHashMap<>();
    private List<TextureAtlasSprite> spritesNeedingUpload = new CopyOnWriteArrayList<>();
    private boolean atlasNeedsExpansion;
    private Lock spriteLoadingLock = new ReentrantLock();

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
        initMissingImage();
        deleteGlTexture();

        int maximumTextureSize = Minecraft.getGLMaximumTextureSize();

        stitcher = new DynamicStitcher(maximumTextureSize, maximumTextureSize, 0, mipmapLevels);

        mapUploadedSprites.clear();
        listAnimatedSprites.clear();

        missingImage.generateMipmaps(mipmapLevels);

        LOGGER.info("Created: {}x{} {}-atlas", stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), basePath);

        TextureUtil.allocateTextureImpl(getGlTextureId(), mipmapLevels, stitcher.getCurrentWidth(), stitcher.getCurrentHeight());
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

    protected TextureAtlasSprite loadSprite(String iconName) {
//        LOGGER.info("Loading texture " + iconName);

        TextureAtlasSprite sprite = mapRegisteredSprites.get(iconName);
        if (sprite == null) {
            sprite = new TextureAtlasSprite(iconName);
        }

        // Load the sprite
        ResourceLocation location = getResourceLocation(sprite);

        if (sprite.hasCustomLoader(resourceManager, location)) {
            sprite.load(resourceManager, location, l -> getAtlasSprite(l.toString()));
        } else if (!iconName.equals("missingno")) {
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
        int oldWidth = stitcher.getCurrentWidth();
        int oldHeight = stitcher.getCurrentHeight();

        stitcher.addSprite(sprite);

        // Texture map got resized, recreate it and upload all textures
        if (stitcher.getCurrentWidth() != oldWidth || stitcher.getCurrentHeight() != oldHeight) {
            atlasNeedsExpansion = true;
        }

        // Upload the texture
        spritesNeedingUpload.add(sprite);

        // Add animated sprites to a list so that it's ticked every tick
        if (sprite.hasAnimationMetadata()) {
            listAnimatedSprites.add(sprite);
        }

        // Updade if calling from Minecraft thread so that inventory items don't flicker
        if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            update();
        }

        return sprite;
    }

    public void update() {
        Minecraft.getMinecraft().profiler.startSection("updateTextureMap");

        if (atlasNeedsExpansion) {
            atlasNeedsExpansion = false;

            Minecraft.getMinecraft().profiler.startSection("expandAtlas");
            LOGGER.info("Expanding texture atlas to {}x{}", stitcher.getCurrentWidth(), stitcher.getCurrentHeight());

            TextureUtil.allocateTextureImpl(getGlTextureId(), mipmapLevels, stitcher.getCurrentWidth(), stitcher.getCurrentHeight());

            GlStateManager.bindTexture(getGlTextureId());
            List<TextureAtlasSprite> slots = stitcher.getStichSlots();

            for (TextureAtlasSprite loadedSprite : slots) {
                TextureUtil.uploadTextureMipmap(loadedSprite.getFrameTextureData(0), loadedSprite.getIconWidth(), loadedSprite.getIconHeight(), loadedSprite.getOriginX(), loadedSprite.getOriginY(), false, false);
            }

            Minecraft.getMinecraft().profiler.endSection();
        }

        Minecraft.getMinecraft().profiler.startSection("uploadTexture");
        GlStateManager.bindTexture(getGlTextureId());
        for (TextureAtlasSprite sprite : spritesNeedingUpload) {
            spritesNeedingUpload.remove(sprite);
            TextureUtil.uploadTextureMipmap(sprite.getFrameTextureData(0), sprite.getIconWidth(), sprite.getIconHeight(), sprite.getOriginX(), sprite.getOriginY(), false, false);
        }
        Minecraft.getMinecraft().profiler.endSection();

        Minecraft.getMinecraft().profiler.endSection();
    }
}
