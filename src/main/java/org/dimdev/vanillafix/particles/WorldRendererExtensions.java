package org.dimdev.vanillafix.particles;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumer;

public interface WorldRendererExtensions {
	//Frustum getFrustum();

	//static void renderIfVisible(Particle particle, VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
	//	if (((WorldRendererExtensions) MinecraftClient.getInstance().worldRenderer).getFrustum().isVisible(particle.getBoundingBox())) {
	//		particle.buildGeometry(vertexConsumer, camera, tickDelta);
	//	}
	//}
}
