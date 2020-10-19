package org.dimdev.vanillafix.util.config;

import blue.endless.jankson.Comment;

public class ModConfig {
    public BugFixes bugFixes = new BugFixes();

    public ClientOnly clientOnly = new ClientOnly();

    public AntiCheat antiCheat = new AntiCheat();

    public static class BugFixes {
        @Comment("Disables loading the spawn chunks when the server starts. This drastically reduces world loading times, but can also cause invisible chunks to appear for the first few seconds.")
        public boolean disableInitialChunkLoad = true;

        @Comment("Compare items by item type rather than NBT when looking for items for the crafting recipe. Fixes https://bugs.mojang.com/browse/MC-129057")
        public boolean fixRecipeBookIngredientsWithTags = true;

        @Comment("Updates the fall distance before notifying the block fallen upon that the entity has fallen on it")
        public boolean updateFallDistance = true;
        @Comment("Fixes a bug where the stone shore biome has a different water color than other coastal cold biomes")
        
        public boolean fixStoneShoreColors = true;
    }

    public static class ClientOnly {
        @Comment("Optimizes animated textures by ticking only visible textures")
        public boolean optimizedAnimatedTextures = true;

        @Comment("Allows opening screens when inside a nether portal")
        public boolean screenInNetherPortal = true;

        @Comment("Set the profilers location to \"gui\" from \"texture\" when running gui logic")
        public boolean splitScreenAndTextureProfiler = true;

        @Comment("Makes interdimensional teleportation nearly as fast as same-dimension teleportation by removing the \"Downloading terrain...\" screen.")
        public boolean fastInterdimensionalTeleportation = true;

        @Comment("Prevents showing particles that can not be seen")
        public boolean cullParticles = true;
    }

    public static class AntiCheat {
        @Comment("Prevents players from stepping up one block (stepping does not reduce hunger)")
        public boolean fixStepHeight = true;

        @Comment("Prevents players from being invulnerable during or after a teleport to a new dimension")
        public boolean noPlayerInvulnerabilityAfterTeleport = true;
    }
}
