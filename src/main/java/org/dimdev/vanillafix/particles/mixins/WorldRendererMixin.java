package org.dimdev.vanillafix.particles.mixins;

import org.dimdev.vanillafix.particles.WorldRendererExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public class WorldRendererMixin implements WorldRendererExtensions {
	@Unique
	private Frustum frustum;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Frustum;setPosition(DDD)V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	public void intercept(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci, Profiler profiler, Vec3d vec3d, double d, double e, double f, Matrix4f matrix4f2, boolean bl, Frustum frustum2) {
		this.frustum = frustum2;
	}

	@Override
	public Frustum getFrustum() {
		return this.frustum;
	}
}
