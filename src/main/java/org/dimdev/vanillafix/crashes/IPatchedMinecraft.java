package org.dimdev.vanillafix.crashes;

import net.minecraft.crash.CrashReport;

public interface IPatchedMinecraft {
    void clearCurrentReport();
    boolean shouldCrashIntegratedServerNextTick();
    void makeErrorNotification(CrashReport report);
}
