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

    @Overwrite
    public void updateAnimations() {
        GlStateManager.bindTexture(getGlTextureId());
        Set<TextureAtlasSprite> visibleTextures = new HashSet<>();
        for (RenderGlobal.ContainerLocalRenderInformation renderInfo : Minecraft.getMinecraft().renderGlobal.renderInfos) {
            visibleTextures.addAll(((IPatchedCompiledChunk) renderInfo.renderChunk.compiledChunk).getVisibleTextures());
        }

        for (TextureAtlasSprite texture : listAnimatedSprites) {
            if (visibleTextures.contains(texture)) {
                Minecraft.getMinecraft().mcProfiler.startSection(texture.getIconName());
                texture.updateAnimation();
                Minecraft.getMinecraft().mcProfiler.endSection();
            }
        }
    }
}
