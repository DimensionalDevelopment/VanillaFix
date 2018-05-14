package org.dimdev.vanillafix.mixins.client;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.dimdev.vanillafix.IPatchedCompiledChunk;
import org.dimdev.vanillafix.TemporaryStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockFluidRenderer.class)
public class MixinBlockFluidRenderer {
    @Inject(method = "renderFluid", at = @At(value = "CONSTANT", args = "floatValue=0.001", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    public void afterTextureDetermined(
            IBlockAccess blockAccess, IBlockState blockStateIn, BlockPos blockPosIn, BufferBuilder bufferBuilderIn, CallbackInfoReturnable<Boolean> cir,
            BlockLiquid blockliquid, boolean flag, TextureAtlasSprite[] atextureatlassprite, int i, float f, float f1, float f2, boolean flag1,
            boolean flag2, boolean[] aboolean, boolean flag3, float f3, float f4, float f5, float f6, Material material, float f7, float f8, float f9,
            float f10, double d0, double d1, double d2, float f11, float f12, TextureAtlasSprite texture) {
        ((IPatchedCompiledChunk) TemporaryStorage.currentCompiledChunk.get(Thread.currentThread().getId())).getVisibleTextures().add(texture);
    }
}
