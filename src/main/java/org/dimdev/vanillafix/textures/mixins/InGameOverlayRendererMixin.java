package org.dimdev.vanillafix.textures.mixins;

import org.dimdev.vanillafix.textures.SpriteExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {
    @Inject(method = "renderFireOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getTextureManager()Lnet/minecraft/client/texture/TextureManager;"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void beforeRenderFireInFirstPerson(MinecraftClient mc, MatrixStack matrixStack, CallbackInfo ci, BufferBuilder bufferBuilder, Sprite sprite) {
        ((SpriteExtensions) sprite).setAnimationUpdateRequired(true);
    }

    @Inject(method = "renderInWallOverlay", at = @At("HEAD"))
    private static void beforeRenderFireInFirstPerson(MinecraftClient minecraftClient, Sprite sprite, MatrixStack matrixStack, CallbackInfo ci) {
        ((SpriteExtensions) sprite).setAnimationUpdateRequired(true);
    }
}
