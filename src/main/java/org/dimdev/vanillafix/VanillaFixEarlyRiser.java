package org.dimdev.vanillafix;

import org.spongepowered.asm.mixin.Mixins;

public class VanillaFixEarlyRiser implements Runnable {
    @Override
    public void run() {
        if (VanillaFix.CONFIG.clientOnly.optimizedAnimatedTextures) {
            VanillaFix.LOGGER.debug("Registering Animated Texture Optimization Mixins");
            Mixins.addConfiguration("vanillafix.textures.mixins.json");
        }
    }
}
