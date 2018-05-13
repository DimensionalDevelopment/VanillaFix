package org.dimdev.vanillafix.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP {
    /**
     * @reason Entities should not be invulnerable after dimension change,
     * players could intentionally abuse this. isInvulnerableDimensionChange
     * is now only to prevent teleporting the player again (in the other nether
     * portal before the client had the time to confirm the teleport).
     * @author Runemoro
     */
    @Redirect(method = "isEntityInvulnerable", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isEntityInvulnerable(Lnet/minecraft/util/DamageSource;)Z"))
    public boolean isEntityInvulnerable(EntityPlayer entityPlayer, DamageSource source) {
        return false;
    }
}
