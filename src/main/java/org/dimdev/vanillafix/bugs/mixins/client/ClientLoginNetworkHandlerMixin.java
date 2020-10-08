package org.dimdev.vanillafix.bugs.mixins.client;

import org.dimdev.vanillafix.util.config.MixinConfigCondition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@MixinConfigCondition(category = "clientOnly", key = "fixStuckLoggingInScreen")
@Environment(EnvType.CLIENT)
@Mixin(ClientLoginNetworkHandler.class)
public class ClientLoginNetworkHandlerMixin {
    @Shadow
    @Final
    private ClientConnection connection;

    /**
     * @reason forces reading of the channel when we enable autoread
     * Fixes issue with stuck "Logging in..." screen
     */
    @Redirect(method = "onLoginSuccess", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientLoginNetworkHandler;connection:Lnet/minecraft/network/ClientConnection;"))
    private void onConnectionStateSet() {
    }

    /**
     * @reason forces reading of the channel when we enable autoread
     * Fixes issue with stuck "Logging in..." screen
     */
    @Inject(method = "onLoginSuccess", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;setPacketListener(Lnet/minecraft/network/listener/PacketListener;)V", shift = At.Shift.AFTER))
    private void onNetHandlerSet(LoginSuccessS2CPacket packet, CallbackInfo ci) {
        this.connection.setState(NetworkState.PLAY);
    }
}
