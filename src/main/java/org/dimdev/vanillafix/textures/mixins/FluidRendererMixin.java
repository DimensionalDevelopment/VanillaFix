package org.dimdev.vanillafix.textures.mixins;

import org.dimdev.vanillafix.textures.ChunkDataExtensions;
import org.dimdev.vanillafix.textures.SpriteExtensions;
import org.dimdev.vanillafix.textures.TemporaryStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(FluidRenderer.class)
public class FluidRendererMixin {
	@Shadow
	@Final
	private Sprite[] lavaSprites;

	@Shadow
	@Final
	private Sprite[] waterSprites;

	@Inject(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/tag/Tag;)Z"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void afterTextureDetermined(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, FluidState state, CallbackInfoReturnable<Boolean> cir, boolean bl) {
		Sprite[] sprites = bl ? this.lavaSprites : this.waterSprites;
		ChunkBuilder.ChunkData chunkData = TemporaryStorage.CURRENT_CHUNK_DATA.get();
		if (chunkData != null) {
			((ChunkDataExtensions) chunkData).getVisibleTextures().add(sprites[0]);
			((ChunkDataExtensions) chunkData).getVisibleTextures().add(sprites[1]);
		} else {
			// Called from non-chunk render thread. Unfortunately, the best we can do
			// is assume it's only going to be used once:
			((SpriteExtensions) sprites[0]).setAnimationUpdateRequired(true);
			((SpriteExtensions) sprites[1]).setAnimationUpdateRequired(true);
		}
	}
}
