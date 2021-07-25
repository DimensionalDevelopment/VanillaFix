package org.dimdev.vanillafix.particles.mixins;

import org.dimdev.vanillafix.particles.WorldRendererExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
/*  FIXME: crashes upon loading a world after some chunks are loaded. Root cause appears to be WorldRendererExtensions? https://gist.github.com/wafflecoffee/9aaf789e11639531b14213cd0ea56f65
	@Redirect(method = "renderParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V"))
	public void cull(Particle particle, VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		WorldRendererExtensions.renderIfVisible(particle, vertexConsumer, camera, tickDelta);
	}
*/
}
