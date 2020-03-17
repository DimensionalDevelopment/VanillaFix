package org.dimdev.vanillafix.textures.modsupport.mixins.client;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.dimdev.vanillafix.textures.IPatchedTextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.client.RenderUtil;

@Pseudo
@Mixin(RenderUtil.class)
public class MixinRenderUtil {
    @Inject(method = "putTexturedQuad(Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;DDDDDDLnet/minecraft/util/EnumFacing;IIIIIIZZ)V", at = @At("HEAD"), remap = false)
    private static void onRenderQuad(BufferBuilder renderer, TextureAtlasSprite sprite, double x, double y, double z, double w, double h, double d, EnumFacing face, int r, int g, int b, int a, int light1, int light2, boolean flowing, boolean flipHorizontally, CallbackInfo ci) {
        if (sprite != null)
            ((IPatchedTextureAtlasSprite) sprite).markNeedsAnimationUpdate();
    }

    @Inject(method = "putRotatedQuad(Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;DDDDDLnet/minecraft/util/EnumFacing;IIIIIIZ)V", at = @At("HEAD"), remap = false)
    private static void onRenderRotatedQuad(BufferBuilder renderer, TextureAtlasSprite sprite, double x, double y, double z, double w, double d, EnumFacing rotation, int r, int g, int b, int a, int light1, int light2, boolean flowing, CallbackInfo ci) {
        if (sprite != null)
            ((IPatchedTextureAtlasSprite) sprite).markNeedsAnimationUpdate();
    }
}
