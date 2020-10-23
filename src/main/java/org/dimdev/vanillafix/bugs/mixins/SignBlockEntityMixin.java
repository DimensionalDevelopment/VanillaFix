package org.dimdev.vanillafix.bugs.mixins;

import org.dimdev.vanillafix.VanillaFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin {
    @Redirect(method = "onActivate", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;execute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)I"))
    public int checkActivate(CommandManager commandManager, ServerCommandSource commandSource, String command) {
        if (VanillaFix.config().bugFixes.fixSignCommands) {
            return commandManager.execute(commandSource, command);
        }
        if (!commandSource.getMinecraftServer().areCommandBlocksEnabled()) {
            if (command.length() > 255) {
                return 0;
            }
        }
        return 0;
    }
}
