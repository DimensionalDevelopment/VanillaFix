package org.dimdev.vanillafix.textures.mixins;

import org.dimdev.vanillafix.textures.SpriteExtensions;
import org.dimdev.vanillafix.util.config.ModConfigCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@ModConfigCondition(category = "clientOnly", key = "optimizedAnimatedTextures")
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "renderFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void renderFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, CallbackInfo ci, Sprite fire0, Sprite fire1) {
        ((SpriteExtensions) fire0).setAnimationUpdateRequired(true);
        ((SpriteExtensions) fire1).setAnimationUpdateRequired(true);
    }
}
