package org.dimdev.vanillafix.bugs.mixins;

import org.dimdev.vanillafix.VanillaFix;
import org.dimdev.vanillafix.util.annotation.MixinConfigValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@MixinConfigValue(category = "bugFixes", value = "doNotConsumeFoodOnDeath")
@Mixin(ItemStack.class)
public class ItemStackMixin {
	/**
	 * Prevents consuming food if the entity is dead
	 * Bugs Fixed:
	 * - https://bugs.mojang.com/browse/MC-133218
	 */
	@Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
	public void interceptFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
		if ((!user.isAlive() || user.isDead()) && VanillaFix.config().bugFixes.doNotConsumeFoodOnDeath) {
			cir.setReturnValue((ItemStack) (Object) this);
		}
	}
}
