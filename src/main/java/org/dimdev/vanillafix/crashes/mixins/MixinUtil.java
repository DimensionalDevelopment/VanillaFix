package org.dimdev.vanillafix.crashes.mixins;

import net.minecraft.crash.CrashReport;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Logger;
import org.dimdev.vanillafix.crashes.CrashUtils;
import org.dimdev.vanillafix.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Mixin(Util.class)
public abstract class MixinUtil {

    /**
     * @reason Warn the player (configurable to crash or log too) instead of only logging a
     * message a scheduled task throws an exception. The default vanilla behaviour is dangerous
     * as things will fail silently, making future bugs much harder to solve. In fact, it may
     * actually be a vanilla bug that the client doesn't crash, since they are using the "fatal"
     * log level, which is otherwise used only for problems which crash the game.
     */
    @Overwrite
    @Nullable
    // TODO: Utils shouldn't depend on minecraft, redirect individual calls to runTask instead
    public static <V> V runTask(FutureTask<V> task, Logger logger) {
        task.run();
        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            ModConfig.ProblemAction action = ModConfig.crashes.scheduledTaskAction;

            if (action == ModConfig.ProblemAction.CRASH) {
                CrashUtils.crash(new CrashReport("Error executing task", e));
            } else if (action == ModConfig.ProblemAction.WARNING_SCREEN) {
                CrashUtils.warn(new CrashReport("Error executing task", e));
            } else if (action == ModConfig.ProblemAction.NOTIFICATION) {
                CrashUtils.notify(new CrashReport("Error executing task", e));
            } else if (action == ModConfig.ProblemAction.LOG) {
                logger.fatal("Error executing task", e);
            }
            return null;
        }
    }
}
