package org.dimdev.vanillafix.bugs.mixins;

import org.dimdev.vanillafix.util.annotation.MixinConfigValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@MixinConfigValue(category = "bugFixes", value = "updateFallDistance")
@Mixin(Entity.class)
public class EntityMixin {
	@Shadow
	public float fallDistance;

	/**
	 * @reason Fixes a vanilla bug where the entity's fall distance is not updated before invoking the
	 * block's onLandedUpon when it falls on the ground, meaning that the last fall state update won't
	 * be included in the fall distance.
	 */
	@Inject(method = "fall", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onLandedUpon(Lnet/minecraft/world/World;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;F)V"))
	private void beforeOnLandedUpon(double y, boolean onGroundIn, BlockState state, BlockPos pos, CallbackInfo ci) {
		if (y < 0) this.fallDistance -= y;
	}
}
