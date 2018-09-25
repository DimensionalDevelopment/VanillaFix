package org.dimdev.vanillafix.textures.mixins.client;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import org.dimdev.vanillafix.textures.IPatchedCompiledChunk;
import org.dimdev.vanillafix.textures.IPatchedTextureAtlasSprite;
import org.dimdev.vanillafix.textures.TemporaryStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;
import java.util.Set;
@Mixin(ForgeBlockModelRenderer.class)
public class MixinForgeBlockModelRenderer {
    /**
     * @reason Adds the textures used to render this block to the set of textures in
     * the CompiledChunk.
     */
    @Inject(method = "render", at = @At("HEAD"), remap = false)
    private static void onRender(VertexLighterFlat lighter, IBlockAccess world, IBakedModel model, IBlockState state, BlockPos pos, BufferBuilder wr, boolean checkSides, long rand, CallbackInfoReturnable<Boolean> cir) {
        CompiledChunk compiledChunk = TemporaryStorage.currentCompiledChunk.get();
        if (compiledChunk != null) {
            Set<TextureAtlasSprite> visibleTextures = ((IPatchedCompiledChunk) compiledChunk).getVisibleTextures();
            for (BakedQuad quad : model.getQuads(state, null, rand)) {
                if (quad.getSprite() != null) {
                    visibleTextures.add(quad.getSprite());
                }
            }
            for (EnumFacing side : EnumFacing.values()) {
                List<BakedQuad> quads = model.getQuads(state, side, rand);
                if (!quads.isEmpty() && !checkSides || state.shouldSideBeRendered(world, pos, side)) {
                    for (BakedQuad quad : quads) {
                        if (quad.getSprite() != null) {
                            visibleTextures.add(quad.getSprite());
                        }
                    }
                }
            }
        } else {
            // Called from non-chunk render thread. Unfortunately, the best we can do
            // is assume it's only going to be used once:
            for (BakedQuad quad : model.getQuads(state, null, rand)) {
                if (quad.getSprite() != null) {
                    ((IPatchedTextureAtlasSprite) quad.getSprite()).markNeedsAnimationUpdate();
                }
            }
            for (EnumFacing side : EnumFacing.values()) {
                List<BakedQuad> quads = model.getQuads(state, side, rand);
                if (!quads.isEmpty() && !checkSides || state.shouldSideBeRendered(world, pos, side)) {
                    for (BakedQuad quad : quads) {
                        if (quad.getSprite() != null) {
                            ((IPatchedTextureAtlasSprite) quad.getSprite()).markNeedsAnimationUpdate();
                        }
                    }
                }
            }
        }
    }
}