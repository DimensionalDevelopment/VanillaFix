package org.dimdev.vanillafix.bugs.mixins.client;

import org.dimdev.vanillafix.util.config.ModConfigCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@ModConfigCondition(category = "clientOnly", key = "screenInNetherPortal")
@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    /**
     * @reason Enables opening GUIs in nether portals. (see https://bugs.mojang.com/browse/MC-2071)
     * This works by making minecraft think that GUI pauses the game
     */
    @Redirect(method = "updateNausea", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;isPauseScreen()Z"))
    private boolean onPauseCheck(Screen guiScreen) {
        return true;
    }
}
