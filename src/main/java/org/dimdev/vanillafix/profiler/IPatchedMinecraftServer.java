package org.dimdev.vanillafix.profiler;

import net.minecraft.profiler.Profiler;

import java.util.List;

public interface IPatchedMinecraftServer {
    List<Profiler.Result> getLastProfilerData();
    void setProfilerName(String profilerName);
    String getProfilerName();
}
