package org.dimdev.vanillafix.bugs.mixins;

import org.dimdev.vanillafix.util.annotation.MixinConfigValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Disables running commands that are longer than 255 characters from a sign
 * when command blocks are disabled on the server.
 * <p>
 * Bugs Fixed:
 * - https://bugs.mojang.com/browse/MC-190478
 */
@MixinConfigValue(category = "bugFixes", value = "fixSignCommands")
@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin {
	@Redirect(method = "onActivate", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;execute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)I"))
	public int checkActivate(CommandManager commandManager, ServerCommandSource commandSource, String command) {
		if (!commandSource.getMinecraftServer().areCommandBlocksEnabled()) {
			if (command.length() > 255) {
				return 0;
			}
		}
		return 0;
	}
}
