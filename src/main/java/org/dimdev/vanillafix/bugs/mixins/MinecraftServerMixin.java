package org.dimdev.vanillafix.bugs.mixins;

import org.dimdev.vanillafix.util.config.ModConfigCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;

@ModConfigCondition(category = "bugFixes", key = "disableInitialChunkLoad")
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    /**
     * @reason Disable initial chunk load. This makes world load much faster, but in exchange
     * the player may see incomplete chunks (like when teleporting to a new area).
     * @author ?
     */
    @Overwrite
    private void prepareStartRegion(WorldGenerationProgressListener worldGenerationProgressListener) {
    }
}
