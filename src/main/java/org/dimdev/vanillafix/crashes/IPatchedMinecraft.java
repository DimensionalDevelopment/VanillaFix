package org.dimdev.vanillafix.crashes;

public interface IPatchedMinecraft {
    void clearCurrentReport();
    boolean isCrashIntegratedServerNextTick();
}
