package org.dimdev.vanillafix.textures.modsupport.mixins.client;

import morph.avaritia.client.render.item.WrappedItemRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import org.dimdev.vanillafix.textures.IPatchedTextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(WrappedItemRenderer.class)
public class MixinWrappedItemRenderer {
    @Inject(method = "renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/item/ItemStack;F)V", at = @At("HEAD"), remap = false)
    private static void onRenderModel(IBakedModel model, ItemStack stack, float alphaOverride, CallbackInfo ci) {
        List<BakedQuad> quads = model.getQuads(null, null, 0);
        for (BakedQuad quad : quads) {
            if (quad.getSprite() != null) {
                ((IPatchedTextureAtlasSprite) quad.getSprite()).markNeedsAnimationUpdate();
            }
        }
    }
}
