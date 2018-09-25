package org.dimdev.vanillafix.textures.mixins.client;

import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.dimdev.vanillafix.textures.IPatchedCompiledChunk;
import org.dimdev.vanillafix.textures.IPatchedTextureAtlasSprite;
import org.dimdev.vanillafix.textures.TemporaryStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockFluidRenderer.class)
public class MixinBlockFluidRenderer {
    /**
     * @reason Adds liquid textures to the set of visible textures in the compiled chunk. Note
     * that this is necessary only for liquid textures, since Forge liquids are rendered by the
     * normal block rendering code.
     */
    @ModifyVariable(method = "renderFluid", at = @At(value = "CONSTANT", args = "floatValue=0.001", ordinal = 1), ordinal = 0)
    private TextureAtlasSprite afterTextureDetermined(TextureAtlasSprite texture) {
        CompiledChunk compiledChunk = TemporaryStorage.currentCompiledChunk.get();
        if (compiledChunk != null) {
            ((IPatchedCompiledChunk) compiledChunk).getVisibleTextures().add(texture);
        } else {
            // Called from non-chunk render thread. Unfortunately, the best we can do
            // is assume it's only going to be used once:
            ((IPatchedTextureAtlasSprite) texture).markNeedsAnimationUpdate();
        }

        return texture;
    }
}
