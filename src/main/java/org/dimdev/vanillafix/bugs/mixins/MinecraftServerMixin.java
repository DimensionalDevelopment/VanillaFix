package org.dimdev.vanillafix.bugs.mixins;

import org.dimdev.vanillafix.VanillaFix;
import org.dimdev.vanillafix.util.annotation.MixinConfigValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;

@MixinConfigValue(category = "bugFixes", value = "disableInitialChunkLoad")
@Mixin(value = MinecraftServer.class, priority = 1)
public class MinecraftServerMixin {
	/**
	 * @reason Disable initial chunk load. This makes world load much faster, but in exchange
	 * the player may see incomplete chunks (like when teleporting to a new area).
	 * @author ?
	 */
	@Inject(method = "prepareStartRegion", at = @At("HEAD"), cancellable = true)
	private void prepareStartRegion(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
		if (VanillaFix.config().bugFixes.disableInitialChunkLoad) {
			ci.cancel();
		}
	}
}
