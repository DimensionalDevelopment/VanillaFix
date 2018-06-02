package org.dimdev.vanillafix.crashes;

import net.minecraft.crash.CrashReport;

public interface IPatchedMinecraft {
    boolean shouldCrashIntegratedServerNextTick();
    void makeErrorNotification(CrashReport report);
}
