package org.dimdev.vanillafix.bugs.mixins.client;

import org.dimdev.vanillafix.util.annotation.MixinConfigValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Makes interdimensional teleportation nearly as fast as same-dimension
 * teleportation by removing the "Downloading terrain..." screen. This will cause
 * the player to see partially loaded terrain rather than waiting for the whole
 * render distance to load, but that's also the vanilla behaviour for same-dimension
 * teleportation.
 */
@MixinConfigValue(category = "clientOnly", value = "fastInterdimensionalTeleportation")
@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@ModifyArg(method = "onGameJoin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
	private Screen onGuiDisplayJoin(Screen original) {
		return null;
	}

	@ModifyArg(method = "onPlayerRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", ordinal = 0))
	private Screen onGuiDisplayRespawn(Screen original) {
		return null;
	}
}
