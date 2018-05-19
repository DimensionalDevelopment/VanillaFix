package org.dimdev.vanillafix.mixins.core.client;

import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import org.dimdev.vanillafix.TemporaryStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RenderChunk.class)
public class MixinRenderChunk {
    /**
     * @reason Store the chunk currently being rebuild in TemporaryStorage.currentCompiledChunk
     * by thread ID (there are multiple chunk renderer threads working at once).
     */
    @Inject(method = "rebuildChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/CompiledChunk;<init>()V", ordinal = 0, shift = At.Shift.BY, by = 2), locals = LocalCapture.CAPTURE_FAILHARD)
    private void initCompiledChunk(float x, float y, float z, ChunkCompileTaskGenerator generator, CallbackInfo ci, CompiledChunk compiledChunk) {
        TemporaryStorage.currentCompiledChunk.put(Thread.currentThread().getId(), compiledChunk);
    }
}
