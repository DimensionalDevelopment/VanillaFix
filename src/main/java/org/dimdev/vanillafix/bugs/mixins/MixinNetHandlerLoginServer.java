package org.dimdev.vanillafix.bugs.mixins;

import net.minecraft.server.network.NetHandlerLoginServer;
import org.dimdev.vanillafix.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(NetHandlerLoginServer.class)
public class MixinNetHandlerLoginServer {

    /**
     * Increases server timeout when connecting to prevent
     * issues with logging in with mods installed
     *
     * @return Timeout limit increase
     */
    //@ModifyConstant(method = "update", constant = @Constant(intValue = 600, ordinal = 0))
    //public int update(int value) { return ModConfig.options.loginTimeout; }

}
