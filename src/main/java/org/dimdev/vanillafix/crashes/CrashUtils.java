package org.dimdev.vanillafix.crashes;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CrashUtils {
    private static Logger log = LogManager.getLogger("VF");

    public static void crash(CrashReport report) {
        throw new ReportedException(report);
    }

    public static void warn(CrashReport report) {
        if (isClient()) {
            // TODO: what if there's several exceptions in a row?
            outputReport(report);
            Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(new GuiCrashScreen(report, true)));
        } else {
            log.fatal(report.getDescription(), report.getCrashCause());
        }
    }

    public static void outputReport(CrashReport report) {
        try {
            if (report.getFile() == null) {
                String reportName = "crash-";
                reportName += new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
                reportName += Minecraft.getMinecraft().isCallingFromMinecraftThread() ? "-client" : "-server";
                reportName += ".txt";

                File reportsDir = isClient() ? new File(Minecraft.getMinecraft().mcDataDir, "crash-reports") : new File("crash-reports");
                File reportFile = new File(reportsDir, reportName);

                report.saveToFile(reportFile);
            }
        } catch (Throwable e) {
            log.fatal("Failed saving report", e);
        }

        log.fatal("Minecraft ran into a problem! " + (report.getFile() != null ? "Report saved to: " + report.getFile() : "Crash report could not be saved.")
                + "\n" + report.getCompleteReport());
    }

    private static boolean isClient() {
        try {
            Class.forName("net.minecraft.client.Minecraft");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
