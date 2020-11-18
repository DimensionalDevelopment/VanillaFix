package org.dimdev.vanillafix.bugs.mixins.invulnerable;

import org.dimdev.vanillafix.util.annotation.MixinConfigValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.network.ServerPlayerEntity;

@MixinConfigValue(category = "antiCheat", value = "noPlayerInvulnerabilityAfterTeleport")
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    /**
     * @reason Entities should not be invulnerable after dimension change, players could
     * intentionally abuse this. isInvulnerableDimensionChange is now only to prevent
     * teleporting the player again (in the other nether portal before the client had the
     * time to confirm the teleport).
     */
    @Redirect(method = "isInvulnerableTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isInTeleportationState()Z"))
    private boolean isEntityInvulnerable(ServerPlayerEntity player) {
        return false;
    }
}
