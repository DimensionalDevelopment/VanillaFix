package org.dimdev.vanillafix.bugs.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;

/**
 * Prevents launching of living entities that are in the same position
 * as an enderdragon.
 * Bugs fixed:
 * - 
 */
@Mixin(EnderDragonEntity.class)
public class EnderDragonEntityMixin {
    @Redirect(method = "launchLivingEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void checkLaunch(Entity entity, double deltaX, double deltaY, double deltaZ) {
        if (deltaX != 0 && deltaY != 0 && deltaZ != 0) {
            entity.addVelocity(deltaX, deltaY, deltaZ);
        }
    }
}
