package org.dimdev.vanillafix.bugs.mixins;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = 500)
public abstract class MixinEntity {
    @Shadow public float fallDistance;
    @Shadow public World world;

    /**
     * @reason Fixes a vanilla bug where the entity's fall distance is not updated before invoking the
     * block's onFallenUpon when it falls on the ground, meaning that the last fall state update won't
     * be included in the fall distance.
     */
    @Inject(method = "updateFallState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onFallenUpon(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;F)V"))
    private void beforeOnFallenUpon(double y, boolean onGroundIn, IBlockState state, BlockPos pos, CallbackInfo ci) {
        if (y < 0) fallDistance -= y;
    }
}
