package org.dimdev.vanillafix.util.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry.Category;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry.Gui.NoTooltip;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry.Gui.RequiresRestart;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "vanillafix")
public class ModConfig implements ConfigData {
    @Category("bugFixes")
    public BugFixes bugFixes = new BugFixes();

    @Category("clientOnly")
    public ClientOnly clientOnly = new ClientOnly();

    @Category("antiCheat")
    public AntiCheat antiCheat = new AntiCheat();

    public static class BugFixes {
        @Comment("Disables loading the spawn chunks when the server starts. This drastically reduces world loading times, but can also cause invisible chunks to appear for the first few seconds.")
        @NoTooltip
        @RequiresRestart
        public boolean disableInitialChunkLoad = true;

        @Comment("Compare items by item type rather than NBT when looking for items for the crafting recipe. Fixes https://bugs.mojang.com/browse/MC-129057")
        @NoTooltip
        @RequiresRestart
        public boolean fixRecipeBookIngredientsWithTags = true;

        @Comment("Updates the fall distance before notifying the block fallen upon that the entity has fallen on it")
        @NoTooltip
        @RequiresRestart
        public boolean updateFallDistance = true;

        @Comment("Fixes a bug where the stone shore biome has a different water color than other coastal cold biomes")
        @NoTooltip
        @RequiresRestart
        public boolean fixStoneShoreColors = true;
    }

    public static class ClientOnly {
        @Comment("Optimizes animated textures by ticking only visible textures")
        @NoTooltip
        @RequiresRestart
        public boolean optimizedAnimatedTextures = true;

        @Comment("Allows opening screens when inside a nether portal")
        @NoTooltip
        @RequiresRestart
        public boolean screenInNetherPortal = true;

        @Comment("Set the profilers location to \"gui\" from \"texture\" when running gui logic")
        @NoTooltip
        @RequiresRestart
        public boolean splitScreenAndTextureProfiler = true;

        @Comment("Makes interdimensional teleportation nearly as fast as same-dimension teleportation by removing the \"Downloading terrain...\" screen.")
        @NoTooltip
        @RequiresRestart
        public boolean fastInterdimensionalTeleportation = true;
    }

    public static class AntiCheat {
        @Comment("Prevents players from stepping up one block (stepping does not reduce hunger)")
        @NoTooltip
        @RequiresRestart
        public boolean fixStepHeight = true;

        @Comment("Prevents players from being invulnerable during or after a teleport to a new dimension")
        @NoTooltip
        @RequiresRestart
        public boolean noPlayerInvulnerabilityAfterTeleport = true;
    }
}
