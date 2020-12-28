package org.dimdev.vanillafix.util.config;

import blue.endless.jankson.Comment;
import org.dimdev.vanillafix.util.annotation.Exclude;

public class ModConfig {
	public General general = new General();

	public BugFixes bugFixes = new BugFixes();

	public ClientOnly clientOnly = new ClientOnly();

	@Exclude
	public AntiCheat antiCheat = new AntiCheat();

	public static class General {
		@Comment("Improve the profiler by splitting it into more sections")
		public boolean profilerImprovements = true;
	}

	public static class BugFixes {
		@Comment("Disables loading the spawn chunks when the server starts. This drastically reduces world loading times. As a side effect, invisible chunks may appear for the first few seconds when creating a new world")
		public boolean disableInitialChunkLoad = true;

		@Comment("Compare items by item type rather than NBT when looking for items for the crafting recipe")
		public boolean fixRecipeBookIngredientsWithTags = true;

		@Comment("Updates the fall distance before notifying the block fallen upon that the entity has fallen on it")
		public boolean updateFallDistance = true;

		@Comment("Fixes a bug where the stone shore biome has a different water color than other coastal cold biomes")
		public boolean fixStoneShoreColors = true;

		@Comment("Sets the mob cap for phantoms. Setting this to any negative number will disable phantom check")
		public int phantomMobCap = -1;

		@Comment("Prevent placing sugarcane underwater.")
		public boolean underwaterSugarcaneFix = true;

		@Comment("Prevents consuming of food that is being eaten on death when keepInventory is enabled")
		public boolean doNotConsumeFoodOnDeath = true;

		@Comment("Prevents running commands longer than 255 characters from a sign")
		public boolean fixSignCommands = true;
	}

	public static class ClientOnly {
		@Comment("Optimizes animated textures by ticking only visible textures. Disabled by default as it currently has quite a few issues")
		public boolean optimizedAnimatedTextures = false;

		@Comment("Allows opening screens when inside a nether portal")
		public boolean screenInNetherPortal = true;

		@Comment("Set the profilers location to \"gui\" from \"texture\" when running gui logic")
		public boolean splitScreenAndTextureProfiler = true;

		@Comment("Makes interdimensional teleportation nearly as fast as same-dimension teleportation by removing the \"Downloading terrain...\" screen")
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
