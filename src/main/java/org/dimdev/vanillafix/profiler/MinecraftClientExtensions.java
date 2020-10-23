package org.dimdev.vanillafix.profiler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface MinecraftClientExtensions {
    void toggleProfiler();
}
