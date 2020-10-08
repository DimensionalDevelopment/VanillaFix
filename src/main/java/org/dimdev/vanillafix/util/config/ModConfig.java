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

    @ConfigEntry.Category("clientOnly")
    public ClientOnly clientOnly = new ClientOnly();

    public static class BugFixes {
        @Comment("Disables loading the spawn chunks when the server starts. This drastically reduces world loading times, but can also cause invisible chunks to appear for the first few seconds.")
        @ConfigEntry.Gui.NoTooltip
        @ConfigEntry.Gui.RequiresRestart
        public boolean disableInitialChunkLoad = true;

        @Comment("Compare items by item type rather than NBT when looking for items for the crafting recipe. Fixes https://bugs.mojang.com/browse/MC-129057")
        @ConfigEntry.Gui.NoTooltip
        @ConfigEntry.Gui.RequiresRestart
        public boolean fixRecipeBookIngredientsWithTags = true;
    }

    public static class ClientOnly {
        @Comment("Optimizes animated textures by ticking only visible textures")
        @ConfigEntry.Gui.NoTooltip
        @ConfigEntry.Gui.RequiresRestart
        public boolean optimizedAnimatedTextures = true;
    }
}