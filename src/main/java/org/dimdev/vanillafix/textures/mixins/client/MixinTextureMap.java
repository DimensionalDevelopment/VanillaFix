package org.dimdev.vanillafix.textures.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import org.dimdev.vanillafix.textures.ChunkSpriteStorage;
import org.dimdev.vanillafix.textures.IPatchedTextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin (TextureMap.class)
public abstract class MixinTextureMap extends AbstractTexture {
    @SuppressWarnings ("ShadowModifiers" /*(AT)*/)
    @Shadow
    @Final
    private List<TextureAtlasSprite> listAnimatedSprites;

    /**
     * @reason Replaces the updateAnimations method to only tick animated textures
     * that are in one of the loaded RenderChunks. This can lead to an FPS more than
     * three times higher on large modpacks with many textures.
     * <p>
     * Also breaks down the "root.tick.textures" profiler by texture name.
     */
    @Overwrite
    public void updateAnimations () {
        Minecraft.getMinecraft ().profiler.startSection ("determineVisibleTextures");
        ChunkSpriteStorage.markAnimationsForUpdate ();
        Minecraft.getMinecraft ().profiler.endSection ();

        GlStateManager.bindTexture (getGlTextureId ());
        for (TextureAtlasSprite texture : listAnimatedSprites) {
            if (((IPatchedTextureAtlasSprite) texture).needsAnimationUpdate ()) {
                Minecraft.getMinecraft ().profiler.startSection (texture.getIconName ());
                texture.updateAnimation ();
                ((IPatchedTextureAtlasSprite) texture).unmarkNeedsAnimationUpdate (); // Can't do this from updateAnimation mixin, that method can be overriden
                Minecraft.getMinecraft ().profiler.endSection ();
            }
        }
    }
}
