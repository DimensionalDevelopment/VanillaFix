package org.dimdev.vanillafix.textures.mixins.client;

import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.vanillafix.textures.ChunkSpriteStorage;
import org.dimdev.vanillafix.textures.IPatchedCompiledChunk;
import org.dimdev.vanillafix.textures.TemporaryStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin (RenderChunk.class)
public class MixinRenderChunk {
    @Shadow
    public CompiledChunk compiledChunk;
    @Shadow
    @Final
    private BlockPos.MutableBlockPos position;
    private boolean animationsAdded = false;
    private static final Logger vanillaFixLog = LogManager.getLogger ("VanillaFix RenderChunk");

    /**
     * @reason Store the chunk currently being rebuild in TemporaryStorage.currentCompiledChunk
     * by thread ID (there are multiple chunk renderer threads working at once).
     */
    @Inject (method = "rebuildChunk", at = @At (value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/CompiledChunk;<init>()V", ordinal = 0, shift = At.Shift.BY, by = 2), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onRebuildChunk (float x, float y, float z, ChunkCompileTaskGenerator generator, CallbackInfo ci, CompiledChunk compiledChunk) {
        TemporaryStorage.currentCompiledChunk.set (compiledChunk);
    }

    @Inject (method = "setCompiledChunk", at = @At ("HEAD"))
    private void onSetCompiledChunkPre (CompiledChunk compiledChunkIn, CallbackInfo ci) {
        removeAnimationsIfNeccessary ();
    }

    private void addAnimationsIfNeccessary () {
        if (!animationsAdded) {
            for (TextureAtlasSprite sprite : ((IPatchedCompiledChunk) compiledChunk).getVisibleTextures ()) {
                ChunkSpriteStorage.addUsage (sprite);
            }
            animationsAdded = true;
        }
    }

    private void removeAnimationsIfNeccessary () {
        if ((compiledChunk != null) && animationsAdded) {
            for (TextureAtlasSprite sprite : ((IPatchedCompiledChunk) compiledChunk).getVisibleTextures ()) {
                ChunkSpriteStorage.removeUsage (sprite);
            }
            animationsAdded = false;
        }
    }

    @Inject (method = "setCompiledChunk", at = @At ("RETURN"))
    private void onSetCompiledChunkPost (CompiledChunk compiledChunkIn, CallbackInfo ci) {
        addAnimationsIfNeccessary ();
    }

    @Inject (method = "stopCompileTask", at = @At ("HEAD"))
    private void onStopCompileTask (CallbackInfo ci) {
        removeAnimationsIfNeccessary ();
    }
}
