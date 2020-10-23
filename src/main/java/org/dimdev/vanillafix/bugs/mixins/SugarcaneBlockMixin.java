package org.dimdev.vanillafix.bugs.mixins;

import org.dimdev.vanillafix.VanillaFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

/**
 * Fixes a bug where sugarcane can be placed underwater
 * when there is water adjacent to them.
 * Bugs Fixed:
 * - https://bugs.mojang.com/browse/MC-929
 */
@Mixin(SugarCaneBlock.class)
public class SugarcaneBlockMixin {
    @Inject(method = "canPlaceAt", at = @At("HEAD"), cancellable = true)
    public void checkCanPlace(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (world.getFluidState(pos).isIn(FluidTags.WATER) && VanillaFix.config().bugFixes.underwaterSugarcaneFix) {
            cir.setReturnValue(Boolean.FALSE);
        }
    }
}
