package org.dimdev.vanillafix.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings({"unused", "NonConstantFieldWithUpperCaseName", "RedundantThrows"}) // Shadow
@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
    @Redirect(method = "onLivingUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", ordinal = 0))
    public GuiScreen getCurrentScreen(Minecraft mc) {
        return null; // Enable GUIs in portals
    }
}
