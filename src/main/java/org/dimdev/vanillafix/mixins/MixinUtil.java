package org.dimdev.vanillafix.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Logger;
import org.dimdev.vanillafix.CrashUtils;
import org.dimdev.vanillafix.GuiCrashScreen;
import org.dimdev.vanillafix.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Mixin(Util.class)
public final class MixinUtil {
    @Nullable
    @Overwrite
    public static <V> V runTask(FutureTask<V> task, Logger logger) { // TODO: Utils shouldn't depend on minecraft, redirect individual calls to runTask instead
        task.run();
        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            ModConfig.ProblemAction action = ModConfig.crashes.scheduledTaskAction;

            switch (action) {
                case CRASH:
                    CrashUtils.crash(new CrashReport("Error executing task", e));
                    break;
                case WARNING_SCREEN:
                    CrashUtils.warn(new CrashReport("Error executing task", e));
                    break;
                case LOG:
                    logger.fatal("Error executing task", e);
                    break;
            }
            return null;
        }
    }
}
