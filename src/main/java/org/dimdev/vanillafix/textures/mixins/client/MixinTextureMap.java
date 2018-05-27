package org.dimdev.vanillafix.textures.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import org.dimdev.vanillafix.textures.IPatchedCompiledChunk;
import org.dimdev.vanillafix.textures.ModCompatibility;
import org.dimdev.vanillafix.textures.TemporaryStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import team.chisel.client.TextureStitcher;

import java.lang.reflect.Field;
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
        // TODO: Recalculate list after chunk update instead!
        Minecraft.getMinecraft().mcProfiler.startSection("determineVisibleTextures");
        Set<TextureAtlasSprite> visibleTextures = new HashSet<>();
        for (RenderGlobal.ContainerLocalRenderInformation renderInfo : Minecraft.getMinecraft().renderGlobal.renderInfos) {
            visibleTextures.addAll(((IPatchedCompiledChunk) renderInfo.renderChunk.compiledChunk).getVisibleTextures());
        }
        visibleTextures.addAll(TemporaryStorage.texturesUsed);
        TemporaryStorage.texturesUsed.clear();
        ModCompatibility.addDependentTextures(visibleTextures);
        Minecraft.getMinecraft().mcProfiler.endSection();

        GlStateManager.bindTexture(getGlTextureId());
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
