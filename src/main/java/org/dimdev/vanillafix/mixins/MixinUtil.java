package org.dimdev.vanillafix.mixins;

import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Mixin(Util.class)
public final class MixinUtil {
    @Nullable
    @Overwrite
    public static <V> V runTask(FutureTask<V> task, Logger logger) {
        task.run();
        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            // TODO: config option to disable this
            throw new ReportedException(new CrashReport("Error executing task", e));
        }
    }
}
