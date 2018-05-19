package org.dimdev.vanillafix.mixins.core;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow public float fallDistance;
    @Shadow public boolean onGround;
    @Shadow public World world;
    @Shadow private AxisAlignedBB boundingBox;

    /**
     * @reason Fixes a vanilla bug where the entity's fall distance is not updated before invoking the
     * block's onFallenUpon when it falls on the ground, meaning that the last fall state update won't
     * be included in the fall distance.
     */
    @Inject(method = "updateFallState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onFallenUpon(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;F)V"))
    private void beforeOnFallenUpon(double y, boolean onGroundIn, IBlockState state, BlockPos pos, CallbackInfo ci) {
        if (y < 0) fallDistance -= y;
    }

    @Inject(method = "updateFallState", at = @At("HEAD"))
    private void beforeUpdateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos, CallbackInfo ci) {
        onGround = !world.getCollisionBoxes((Entity) (Object) this, boundingBox.expand(0, -0.05, 0)).isEmpty();
    }
}
