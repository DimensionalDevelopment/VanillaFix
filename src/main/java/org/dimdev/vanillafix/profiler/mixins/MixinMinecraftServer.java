package org.dimdev.vanillafix.profiler.mixins;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import org.dimdev.vanillafix.profiler.IPatchedMinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements IPatchedMinecraftServer {
    @Shadow @Final public Profiler profiler;
    private List<Profiler.Result> lastProfilerData = null;
    private String profilerName = "root";

    /**
     * @reason Store profiler data on each tick such that the client can get it
     * without having to call getLastProfilerData (Profiler isn't async).
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void updateLastProfilerData(CallbackInfo ci) {
        if (profiler.profilingEnabled) {
            lastProfilerData = profiler.getProfilingData(profilerName);
        } else {
            lastProfilerData = null;
        }
    }

    @Override
    public List<Profiler.Result> getLastProfilerData() {
        return lastProfilerData;
    }

    @Override
    public void setProfilerName(String profilerName) {
        this.profilerName = profilerName;
    }

    @Override
    public String getProfilerName() {
        return profilerName;
    }

    /**
     * @reason Add a profiler section for the tick limit wait (similar to client's FPS limit wait)
     */
    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;sleep(J)V"))
    private void addTickLimitWaitProfiler(long millis) throws InterruptedException {
        profiler.startSection("root");
        profiler.startSection("tickLimitWait");
        Thread.sleep(millis);
        profiler.endSection();
        profiler.endSection();
    }
}
