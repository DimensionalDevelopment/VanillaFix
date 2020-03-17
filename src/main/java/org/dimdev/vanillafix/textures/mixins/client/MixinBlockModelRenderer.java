package org.dimdev.vanillafix.textures.mixins.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.dimdev.vanillafix.textures.IPatchedCompiledChunk;
import org.dimdev.vanillafix.textures.IPatchedTextureAtlasSprite;
import org.dimdev.vanillafix.textures.TemporaryStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {
    /**
     * @reason Adds the textures used to render this block to the set of textures in
     * the CompiledChunk.
     */
    @Inject(method = "renderModel(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;ZJ)Z", at = @At("HEAD"))
    private void beforeRenderModel(IBlockAccess world, IBakedModel model, IBlockState state, BlockPos pos, BufferBuilder buffer, boolean checkSides, long rand, CallbackInfoReturnable<Boolean> ci) {
        CompiledChunk compiledChunk = TemporaryStorage.currentCompiledChunk.get();
        Set<TextureAtlasSprite> visibleTextures;
        if (compiledChunk != null) {
            visibleTextures = ((IPatchedCompiledChunk) compiledChunk).getVisibleTextures();
        } else {
            // Called from non-chunk render thread. Unfortunately, the best we can do
            // is assume it's only going to be used once:
            visibleTextures = new HashSet<>();
        }

        for (EnumFacing side : EnumFacing.values()) {
            if (!checkSides || state.shouldSideBeRendered(world, pos, side)) {
                List<BakedQuad> quads = model.getQuads(state, side, rand);
                for (BakedQuad quad : quads) {
                    if (quad == null) {
                        continue;
                    } else {
                        quad.getSprite();
                    }
                    visibleTextures.add(quad.getSprite());
                }
            }
        }

        List<BakedQuad> quads = model.getQuads(state, null, rand);
        for (BakedQuad quad : quads) {
            if (quad == null) continue;
            visibleTextures.add(quad.getSprite());
        }

        if (compiledChunk == null) {
            for (TextureAtlasSprite texture : visibleTextures) {
                ((IPatchedTextureAtlasSprite) texture).markNeedsAnimationUpdate();
            }
        }
    }
}
