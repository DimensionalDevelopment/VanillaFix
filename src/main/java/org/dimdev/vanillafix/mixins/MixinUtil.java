package org.dimdev.vanillafix.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Logger;
import org.dimdev.vanillafix.GuiCrashScreen;
import org.dimdev.vanillafix.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
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
            if (ModConfig.crashes.scheduledTaskAction == ModConfig.ProblemAction.WARNING_SCREEN) {
                // TODO: what if there's several exceptions in a row?
                CrashReport report = new CrashReport("Error executing task", e);
                File crashReportsDir = new File(Minecraft.getMinecraft().mcDataDir, "crash-reports");
                File crashReportSaveFile = new File(crashReportsDir, "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-scheduled-task.txt");

                // Print the report in bootstrap
                Bootstrap.printToSYSOUT(report.getCompleteReport());

                // Save the report and print file in bootstrap
                File reportFile = null;
                if (report.getFile() != null) {
                    reportFile = report.getFile();
                } else if (report.saveToFile(crashReportSaveFile)) {
                    reportFile = crashReportSaveFile;
                }

                if (reportFile != null) {
                    Bootstrap.printToSYSOUT("Scheduled task threw an exception! Crash report saved to: " + reportFile);
                } else {
                    Bootstrap.printToSYSOUT("Scheduled task threw an exception! Crash report could not be saved.");
                }

                Minecraft.getMinecraft().displayGuiScreen(new GuiCrashScreen(reportFile, report, true));
            } else if (ModConfig.crashes.scheduledTaskAction == ModConfig.ProblemAction.CRASH) {
                throw new ReportedException(new CrashReport("Error executing task", e));
            } else {
                logger.fatal("Error executing task", e);
            }
            return null;
        }
    }
}
