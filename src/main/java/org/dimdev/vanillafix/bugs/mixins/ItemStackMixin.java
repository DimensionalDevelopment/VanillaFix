package org.dimdev.vanillafix.bugs.mixins;

import org.dimdev.vanillafix.VanillaFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    public void interceptFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if ((!user.isAlive() || user.isDead()) && VanillaFix.config().bugFixes.doNotConsumeFoodOnDeath) {
            cir.setReturnValue((ItemStack) (Object) this);
        }
    }
}
