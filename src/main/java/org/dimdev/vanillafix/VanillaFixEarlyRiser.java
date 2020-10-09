package org.dimdev.vanillafix;

import org.spongepowered.asm.mixin.Mixins;

public class VanillaFixEarlyRiser implements Runnable {
    @Override
    public void run() {
        if (VanillaFix.CONFIG.clientOnly.optimizedAnimatedTextures) {
            Mixins.addConfiguration("vanillafix.textures.mixins.json");
        }
    }
}
