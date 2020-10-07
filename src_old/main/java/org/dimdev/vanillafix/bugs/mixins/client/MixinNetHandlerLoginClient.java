package org.dimdev.vanillafix.bugs.mixins.client;

import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.server.SPacketLoginSuccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerLoginClient.class)
public final class MixinNetHandlerLoginClient {

    @Shadow @Final private NetworkManager networkManager;

    /**
     * @reason forces reading of the channel when we enable autoread
     * Fixes issue with stuck "Logging in..." screen
     */
    @Inject(method = "handleLoginSuccess", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;setConnectionState(Lnet/minecraft/network/EnumConnectionState;)V"), cancellable = true)
    private void onConnectionStateSet(SPacketLoginSuccess packetIn, CallbackInfo ci) {
        ci.cancel();
    }

    /**
     * @reason forces reading of the channel when we enable autoread
     * Fixes issue with stuck "Logging in..." screen
     */
    @Inject(method = "handleLoginSuccess", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;setNetHandler(Lnet/minecraft/network/INetHandler;)V", shift = At.Shift.AFTER))
    private void onNetHandlerSet(SPacketLoginSuccess packetIn, CallbackInfo ci) {
        this.networkManager.setConnectionState(EnumConnectionState.PLAY);
    }
}
