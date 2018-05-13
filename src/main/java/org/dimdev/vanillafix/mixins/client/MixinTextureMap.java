package org.dimdev.vanillafix.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextureMap.class)
public class MixinTextureMap {
    @Redirect(method = "updateAnimations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;updateAnimation()V"))
    public void updateAnimations(TextureAtlasSprite textureAtlasSprite) {
        Minecraft.getMinecraft().mcProfiler.startSection(textureAtlasSprite.getIconName());
        textureAtlasSprite.updateAnimation();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }
}
