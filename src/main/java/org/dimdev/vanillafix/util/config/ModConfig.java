package org.dimdev.vanillafix.util.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

@Config(name = "vanillafix")
public class ModConfig implements ConfigData {
    @ConfigEntry.Category("bugFixes")
    public BugFixes bugFixes = new BugFixes();

    public static class BugFixes {
        @ConfigEntry.Gui.RequiresRestart
        public boolean disableInitialChunkLoad = true;
    }
}
