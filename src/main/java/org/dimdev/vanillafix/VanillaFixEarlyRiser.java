package org.dimdev.vanillafix;

import org.spongepowered.asm.mixin.Mixins;

import net.fabricmc.loader.api.FabricLoader;

public class VanillaFixEarlyRiser implements Runnable {
    @Override
    public void run() {
        if (VanillaFix.config().clientOnly.optimizedAnimatedTextures ||
                // sodium already does this
                !(FabricLoader.getInstance().isModLoaded("sodium"))) {
            VanillaFix.LOGGER.info("Registering Animated Texture Optimization Mixins");
            Mixins.addConfiguration("vanillafix.textures.mixins.json");
        }
        if (VanillaFix.config().clientOnly.cullParticles) {
            VanillaFix.LOGGER.info("Registering Particle Optimization Mixins");
            Mixins.addConfiguration("vanillafix.particles.mixins.json");
        }
    }
}
