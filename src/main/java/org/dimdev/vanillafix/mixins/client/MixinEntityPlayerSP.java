package org.dimdev.vanillafix.mixins.client;

import net.minecraft.client.Minecraft;
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
     * always return null.
     */
    @Redirect(method = "onLivingUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", ordinal = 0))
    private GuiScreen getCurrentScreen(Minecraft mc) {
        return null;
    }
}
