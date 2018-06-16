package org.dimdev.vanillafix.textures.modsupport.mixins.client;

import morph.avaritia.api.IHaloRenderItem;
import morph.avaritia.client.render.item.HaloRenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import org.dimdev.vanillafix.textures.IPatchedTextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(HaloRenderItem.class)
public class MixinHaloRenderItem {
    @Inject(method = "renderItem", at = @At("HEAD"), remap = false)
    private void onRenderItem(ItemStack stack, ItemCameraTransforms.TransformType transformType, CallbackInfo ci) {
        if (stack.getItem() instanceof IHaloRenderItem) {
            ((IPatchedTextureAtlasSprite) ((IHaloRenderItem) stack.getItem()).getHaloTexture(stack)).markNeedsAnimationUpdate();
        }
    }
}
