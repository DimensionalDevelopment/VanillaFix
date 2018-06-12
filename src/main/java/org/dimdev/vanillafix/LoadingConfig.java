package org.dimdev.vanillafix;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

public class LoadingConfig {

    private Configuration config;

    public boolean bugFixes;
    public boolean crashFixes;
    public boolean idLimit;
    public boolean modSupport;
    public boolean profiler;
    public boolean textureFixes;


    public void init(File file) {
        if (!file.exists()) {
            bugFixes = true;
            crashFixes = true;
            idLimit = true;
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
        bugFixes = getBoolean("bugFixes", "fixes", true);
        crashFixes = getBoolean("crashFixes", "fixes", true);
        idLimit = getBoolean("idLimit", "fixes", true);
        modSupport = getBoolean("modSupport", "fixes", true);
        profiler = getBoolean("profiler", "fixes", true);
        textureFixes = getBoolean("textureFixes", "fixes", true);
    }

    private boolean getBoolean(String name, String category, boolean defaultValue) {
        Property prop = config.get(category, name, defaultValue);
        return prop.getBoolean(defaultValue);
    }

}
