package org.dimdev.vanillafix.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ReportedException;
import org.dimdev.vanillafix.IPatchedMinecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Future;

@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer {
    @Shadow @Final private Minecraft mc;

    /**
     * If the everyone leaves the integrated server, and a shutdown is then
     * initiated, there is a possibility that by the time initiateShutdown
     * is called, the server is still running but will shut down before running
     * scheduled tasks. Don't wait for the task to run, since that may never happen.
     */
    @Redirect(method = "initiateShutdown", at = @At(value = "INVOKE", target = "Lcom/google/common/util/concurrent/Futures;getUnchecked(Ljava/util/concurrent/Future;)Ljava/lang/Object;", ordinal = 0))
    public <V> V getUnchecked(Future<V> future) {
        return null;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void beforeTick(CallbackInfo ci) {
        if (((IPatchedMinecraft) mc).isIntegratedServerCrashScheduled()) {
            throw new ReportedException(new CrashReport("Manually triggered server-side debug crash", new Throwable()));
        }
    }
}
