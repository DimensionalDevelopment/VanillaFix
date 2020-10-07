package org.dimdev.vanillafix.bugs.mixins.client;

import io.netty.channel.Channel;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public final class MixinNetworkManager {

    @Shadow private Channel channel;

    /**
     * @reason forces reading of the channel when we enable autoread
     * Fixes issue with stuck "Logging in..." screen
     */
    @Inject(method = "setConnectionState", at = @At(value = "INVOKE", target = "Lio/netty/channel/ChannelConfig;setAutoRead(Z)Lio/netty/channel/ChannelConfig;", shift = At.Shift.AFTER))
    private void onAutoReadEnabled(EnumConnectionState newState, CallbackInfo ci) {
        this.channel.read();
    }
}
