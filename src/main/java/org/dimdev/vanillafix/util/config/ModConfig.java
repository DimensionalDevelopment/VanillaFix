package org.dimdev.vanillafix.util.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@SuppressWarnings("unused") // Reflection
@Config(name = "vanillafix")
public class ModConfig implements ConfigData {
    @ConfigEntry.Category("bugFixes")
    public BugFixes bugFixes = new BugFixes();

    public static class BugFixes {
        @Comment("Disables loading the spawn chunks when the server starts. This drastically reduces world loading times, but can also cause invisible chunks to appear for the first few seconds. ")
        @ConfigEntry.Gui.NoTooltip
        @ConfigEntry.Gui.RequiresRestart
        public boolean disableInitialChunkLoad = true;
    }
}
