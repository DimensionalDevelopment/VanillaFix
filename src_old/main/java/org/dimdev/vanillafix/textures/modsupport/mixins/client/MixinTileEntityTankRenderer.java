package org.dimdev.vanillafix.textures.modsupport.mixins.client;

import net.minecraft.client.renderer.BufferBuilder;
import openblocks.client.renderer.tileentity.TileEntityTankRenderer;
import openblocks.client.renderer.tileentity.tank.ITankRenderFluidData;
import openmods.utils.TextureUtils;
import org.dimdev.vanillafix.textures.IPatchedTextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(TileEntityTankRenderer.class)
public class MixinTileEntityTankRenderer {
    @Inject(method = "renderFluid", at = @At("HEAD"), remap = false)
    private static void onRenderFluid(BufferBuilder wr, ITankRenderFluidData data, float time, int combinedLights, CallbackInfo ci) {
        ((IPatchedTextureAtlasSprite) TextureUtils.getFluidTexture(data.getFluid())).markNeedsAnimationUpdate();
    }
}
