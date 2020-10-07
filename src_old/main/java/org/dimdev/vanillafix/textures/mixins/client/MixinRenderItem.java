package org.dimdev.vanillafix.textures.mixins.client;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.item.ItemStack;
import org.dimdev.vanillafix.textures.IPatchedTextureAtlasSprite;
import org.dimdev.vanillafix.textures.TemporaryStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RenderItem.class)
public class MixinRenderItem {
    /**
     * @reason Keep a list of textures that have been used this frame to render an item
     * such that their animation can be updated next tick.
     */
    @Inject(method = "renderQuads", at = @At("HEAD"))
    public void beforeRenderItem(BufferBuilder renderer, List<BakedQuad> quads, int color, ItemStack stack, CallbackInfo ci) {
        for (BakedQuad quad : quads) {
            if (quad.getSprite() == null) continue;
            ((IPatchedTextureAtlasSprite) quad.getSprite()).markNeedsAnimationUpdate();
        }
    }
}
