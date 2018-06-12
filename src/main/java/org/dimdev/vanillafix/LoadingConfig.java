package org.dimdev.vanillafix;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class LoadingConfig {

    private Configuration config;

    public boolean bugFixes;
    public boolean crashFixes;
    public boolean modSupport;
    public boolean profiler;
    public boolean textureFixes;


    public void init(File file) {
        if (!file.exists()) {
            bugFixes = true;
            crashFixes = true;
            modSupport = true;
            profiler = true;
            textureFixes = true;
            return;
        }
        if (config == null) {
            config = new Configuration(file);
            reload();
        }
    }

    public void reload() {
        bugFixes = config.get("fixes", "bugFixes", true).getBoolean();
        crashFixes = config.get("fixes", "crashFixes", true).getBoolean();
        modSupport = config.get("fixes", "modSupport", true).getBoolean();
        profiler = config.get("fixes", "profiler", true).getBoolean();
        textureFixes = config.get("fixes", "textureFixes", true).getBoolean();
    }
}
