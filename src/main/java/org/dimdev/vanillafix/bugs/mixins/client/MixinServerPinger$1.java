package org.dimdev.vanillafix.bugs.mixins.client;

import net.minecraft.network.status.INetHandlerStatusClient;
import net.minecraft.network.status.server.SPacketPong;
import org.dimdev.vanillafix.bugs.IPatchedSPacketPong;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.network.ServerPinger$1")
public abstract class MixinServerPinger$1 implements INetHandlerStatusClient {
    @Shadow private long pingSentAt;

    @Inject(method = "handlePong", at = @At("HEAD"), cancellable = true)
    public void handleHandlePong(SPacketPong packetIn, CallbackInfo ci) {
        if (((IPatchedSPacketPong) packetIn).getClientTime() != pingSentAt) {
            ci.cancel();
        }
    }
}