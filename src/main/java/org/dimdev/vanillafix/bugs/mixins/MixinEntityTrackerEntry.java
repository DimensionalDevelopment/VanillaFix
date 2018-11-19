package org.dimdev.vanillafix.bugs.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityAttach;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class MixinEntityTrackerEntry {

    @Shadow @Final private Entity trackedEntity;

    @Inject(method = "updatePlayerEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addTrackingPlayer(Lnet/minecraft/entity/player/EntityPlayerMP;)V"))
    public void sendAttachPacketIfNecessary(EntityPlayerMP playerMP, CallbackInfo ci) {
        if (trackedEntity instanceof EntityLiving) {
            playerMP.connection.sendPacket(new SPacketEntityAttach(trackedEntity, ((EntityLiving) trackedEntity).getLeashHolder()));
        }
    }

}
