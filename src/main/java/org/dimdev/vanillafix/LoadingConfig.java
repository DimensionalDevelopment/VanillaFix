package org.dimdev.vanillafix;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class LoadingConfig {
    public boolean bugFixes = true;
    public boolean crashFixes = true;
    public boolean modSupport = true;
    public boolean profiler = true;
    public boolean textureFixes = true;
    public boolean blockStates = true;
    public boolean dynamicResources = true;
    public boolean improvedLaunchWrapper = true;

    public LoadingConfig(File file) {
        if (!file.exists()) {
            return;
        }

        Configuration config = new Configuration(file);
        bugFixes = config.get("fixes", "bugFixes", true).getBoolean();
        crashFixes = config.get("fixes", "crashFixes", true).getBoolean();
        modSupport = config.get("fixes", "modSupport", true).getBoolean();
        profiler = config.get("fixes", "profiler", true).getBoolean();
        textureFixes = config.get("fixes", "textureFixes", true).getBoolean();
        blockStates = config.get("fixes", "blockStates", true).getBoolean();
        dynamicResources = config.get("fixes", "dynamicResources", true).getBoolean();
        improvedLaunchWrapper = config.get("fixes", "improvedLaunchWrapper", true).getBoolean();
    }
}
