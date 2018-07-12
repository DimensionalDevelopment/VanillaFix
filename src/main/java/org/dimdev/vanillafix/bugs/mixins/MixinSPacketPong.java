package org.dimdev.vanillafix.bugs.mixins;

import net.minecraft.network.status.server.SPacketPong;
import org.dimdev.vanillafix.bugs.IPatchedSPacketPong;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SPacketPong.class)
public class MixinSPacketPong implements IPatchedSPacketPong {
    @Shadow long clientTime;
    @Override
    public long getClientTime() {
        return clientTime;
    }
}
