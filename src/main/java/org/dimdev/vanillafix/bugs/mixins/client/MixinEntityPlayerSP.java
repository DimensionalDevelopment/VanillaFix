package org.dimdev.vanillafix.bugs.mixins.client;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
    /**
     * @reason Enables opening GUIs in nether portals. This works by making the
     * vanilla code thinks no GUI is open by forcing Minecraft.currentScreen to
     * always return null. (see https://bugs.mojang.com/browse/MC-2071)
     */
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;doesGuiPauseGame()Z"))
    private boolean onPauseCheck(GuiScreen guiScreen) {
        return false;
    }
}
