package org.dimdev.vanillafix.textures.mixins.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.dimdev.vanillafix.textures.IPatchedCompiledChunk;
import org.dimdev.vanillafix.textures.IPatchedTextureAtlasSprite;
import org.dimdev.vanillafix.textures.TemporaryStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.List;
import java.util.Set;

@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {
    /**
     * @reason Adds the textures used to render this block to the set of textures in
     * the CompiledChunk.
     */
    @Inject(method = "renderQuadsSmooth", at = @At("HEAD"))
    private void onRenderQuadsSmooth(IBlockAccess blockAccess, IBlockState state, BlockPos pos, BufferBuilder buffer, List<BakedQuad> quads, float[] quadBounds, BitSet bitSet, BlockModelRenderer.AmbientOcclusionFace aoFace, CallbackInfo ci) {
        markQuads(quads);
    }

    /**
     * @reason Adds the textures used to render this block to the set of textures in
     * the CompiledChunk.
     */
    @Inject(method = "renderQuadsFlat", at = @At("HEAD"))
    private void onRenderQuadsFlat(IBlockAccess blockAccess, IBlockState state, BlockPos pos, int brightness, boolean ownBrightness, BufferBuilder buffer, List<BakedQuad> quads, BitSet bitSet, CallbackInfo ci) {
        markQuads(quads);
    }

    private static void markQuads(List<BakedQuad> quads) {
        CompiledChunk compiledChunk = TemporaryStorage.currentCompiledChunk.get();
        if (compiledChunk != null) {
            Set<TextureAtlasSprite> visibleTextures = ((IPatchedCompiledChunk) compiledChunk).getVisibleTextures();

            for (BakedQuad quad : quads) {
                if (quad.getSprite() != null) {
                    visibleTextures.add(quad.getSprite());
                }
            }
        } else {
            // Called from non-chunk render thread. Unfortunately, the best we can do
            // is assume it's only going to be used once:
            for (BakedQuad quad : quads) {
                if (quad.getSprite() != null) {
                    ((IPatchedTextureAtlasSprite) quad.getSprite()).markNeedsAnimationUpdate();
                }
            }
        }
    }
}
