package org.dimdev.vanillafix.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import org.dimdev.vanillafix.IPatchedCompiledChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(TextureMap.class)
public abstract class MixinTextureMap extends AbstractTexture {
    @Shadow @Final private List<TextureAtlasSprite> listAnimatedSprites;

    /**
     * @reason Replaces the updateAnimations method to only tick animated textures
     * that are in one of the loaded RenderChunks. This can lead to an FPS more than
     * three times higher on large modpacks with many textures.
     * <p>
     * Also breaks down the "root.tick.textures" profiler by texture name.
     */
    @Overwrite
    public void updateAnimations() {
        GlStateManager.bindTexture(getGlTextureId());
        Set<TextureAtlasSprite> visibleTextures = new HashSet<>();
        for (RenderGlobal.ContainerLocalRenderInformation renderInfo : Minecraft.getMinecraft().renderGlobal.renderInfos) {
            visibleTextures.addAll(((IPatchedCompiledChunk) renderInfo.renderChunk.compiledChunk).getVisibleTextures());
        }

        for (TextureAtlasSprite texture : listAnimatedSprites) {
            // loop through list since HashSet.contains is fast (O(1)) but not ArrayList.contains
            if (visibleTextures.contains(texture)) {
                Minecraft.getMinecraft().mcProfiler.startSection(texture.getIconName());
                texture.updateAnimation();
                Minecraft.getMinecraft().mcProfiler.endSection();
            }
        }
    }
}
