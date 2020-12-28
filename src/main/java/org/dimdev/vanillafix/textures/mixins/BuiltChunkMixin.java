package org.dimdev.vanillafix.textures.mixins;

import org.dimdev.vanillafix.textures.TemporaryStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.chunk.ChunkBuilder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class BuiltChunkMixin {
	@Shadow
	public abstract ChunkBuilder.ChunkData getData();

	@Inject(method = "rebuild", at = @At("HEAD"))
	private void onRebuild(CallbackInfo ci) {
		TemporaryStorage.CURRENT_CHUNK_DATA.set(this.getData());
	}
}
