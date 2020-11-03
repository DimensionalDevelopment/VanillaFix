package org.dimdev.vanillafix;

import org.spongepowered.asm.mixin.Mixins;

import net.fabricmc.loader.api.FabricLoader;

public class VanillaFixEarlyRiser implements Runnable {
    @Override
    public void run() {
        if (VanillaFix.config().clientOnly.optimizedAnimatedTextures ||
                // sodium already does this
                !(FabricLoader.getInstance().isModLoaded("sodium"))) {
            VanillaFix.LOGGER.debug("Registering Animated Texture Optimization Mixins");
            Mixins.addConfiguration("vanillafix.textures.mixins.json");
        }
        if (VanillaFix.config().clientOnly.cullParticles ||
                // sodium already does this too
                !(FabricLoader.getInstance().isModLoaded("sodium"))) {
            VanillaFix.LOGGER.debug("Registering Particle Optimization Mixins");
            Mixins.addConfiguration("vanillafix.particles.mixins.json");
        }
        if (VanillaFix.config().general.profilerImprovements) {
            VanillaFix.LOGGER.debug("Registering Profiler Improvements Mixins");
            Mixins.addConfiguration("vanillafix.profiler.mixins.json");
        }
    }
}
