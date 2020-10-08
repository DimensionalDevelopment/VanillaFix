package org.dimdev.vanillafix.textures.mixins;

import java.util.BitSet;
import java.util.List;
import java.util.Set;

import org.dimdev.vanillafix.textures.ChunkDataExtensions;
import org.dimdev.vanillafix.textures.SpriteExtensions;
import org.dimdev.vanillafix.textures.TemporaryStorage;
import org.dimdev.vanillafix.util.config.ModConfigCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@ModConfigCondition(category = "clientOnly", key = "optimizedAnimatedTextures")
@Mixin(BlockModelRenderer.class)
public class BlockModelRendererMixin {
    @Inject(method = "renderQuadsSmooth", at = @At("HEAD"))
    private void onRenderQuadsSmooth(BlockRenderView world, BlockState state, BlockPos pos, MatrixStack matrix, VertexConsumer vertexConsumer, List<BakedQuad> quads, float[] box, BitSet flags, BlockModelRenderer.AmbientOcclusionCalculator ambientOcclusionCalculator, int overlay, CallbackInfo ci) {
        markQuads(quads);
    }

    @Inject(method = "renderQuadsFlat", at = @At("HEAD"))
    private void onRenderQuadsFlat(BlockRenderView world, BlockState state, BlockPos pos, int light, int overlay, boolean useWorldLight, MatrixStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, BitSet flags, CallbackInfo ci) {
        markQuads(quads);
    }

    @Unique
    private static void markQuads(List<BakedQuad> quads) {
        ChunkBuilder.ChunkData chunkData = TemporaryStorage.CURRENT_CHUNK_DATA.get();
        if (chunkData != null) {
            Set<Sprite> visibleTextures = ((ChunkDataExtensions) chunkData).getVisibleTextures();
            for (BakedQuad quad : quads) {
                if (((BakedQuadAccessor) quad).getSprite() != null) {
                    visibleTextures.add(((BakedQuadAccessor) quad).getSprite());
                }
            }
        } else {
            // Called from non-chunk render thread. Unfortunately, the best we can do
            // is assume it's only going to be used once:
            for (BakedQuad quad : quads) {
                if (((BakedQuadAccessor) quad).getSprite() != null) {
                    ((SpriteExtensions) ((BakedQuadAccessor) quad).getSprite()).setAnimationUpdateRequired(true);
                }
            }
        }
    }
}
