package org.dimdev.vanillafix.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Bootstrap;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.MinecraftError;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;
import org.dimdev.vanillafix.GuiCrashScreen;
import org.dimdev.vanillafix.IPatchedMinecraft;
import org.lwjgl.LWJGLException;
import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings({"unused", "NonConstantFieldWithUpperCaseName", "RedundantThrows"}) // Shadow
@SideOnly(Side.CLIENT)
@Mixin(Minecraft.class)
@Implements(@Interface(iface = IPatchedMinecraft.class, prefix = "minecraft$"))
public abstract class MixinMinecraft implements IThreadListener, ISnooperInfo, IPatchedMinecraft {

    @Shadow @Final private static Logger LOGGER;
    @Shadow volatile boolean running;
    @Shadow private boolean hasCrashed;
    @Shadow private CrashReport crashReporter;
    @Shadow private long debugCrashKeyPressTime;
    @Shadow public WorldClient world;
    @Shadow public static byte[] memoryReserve;
    @Shadow public RenderGlobal renderGlobal;
    @Shadow public GameSettings gameSettings;
    @Shadow public GuiIngame ingameGUI;
    @Shadow public EntityRenderer entityRenderer;

    @Shadow private void init() throws LWJGLException, IOException {}
    @Shadow private void runGameLoop() throws IOException {}
    @Shadow public void displayGuiScreen(@Nullable GuiScreen guiScreenIn) {}
    @Shadow public CrashReport addGraphicsAndWorldToCrashReport(CrashReport theCrash) { return null; }
    @Shadow public void shutdownMinecraftApplet() {}
    @Shadow public void displayCrashReport(CrashReport crashReportIn) {}
    @Shadow public abstract void loadWorld(@Nullable WorldClient worldClientIn);

    private CrashReport currentReport = null;

    /**
     * @author Runemoro
     * @reason Allows the player to choose to return to the title screen after a crash, or get
     * a pasteable link to the crash report on paste.dimdev.org.
     */
    @Overwrite
    public void run() {
        running = true;

        try {
            init();
        } catch (Throwable throwable) {
            CrashReport report = CrashReport.makeCrashReport(throwable, "Initializing game");
            report.makeCategory("Initialization");
            displayCrashReport(addGraphicsAndWorldToCrashReport(report)); // TODO: GUI for this too
            return;
        }

        try {
            while (running) {
                if (!hasCrashed || crashReporter == null) {
                    try {
                        runGameLoop();
                    } catch (ReportedException e) {
                        addGraphicsAndWorldToCrashReport(e.getCrashReport());
                        freeMemory();
                        LOGGER.fatal("Reported exception thrown!", e);
                        //displayCrashScreen(e.getCrashReport());
                    } catch (Throwable e) {
                        CrashReport report = addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", e));
                        freeMemory();
                        LOGGER.fatal("Unreported exception thrown!", e);
                        displayCrashScreen(report);
                    }
                } else {
                    displayCrashReport(crashReporter);
                }
            }
        } catch (MinecraftError ignored) {
        } finally {
            shutdownMinecraftApplet();
        }
    }

    public void displayCrashScreen(CrashReport report) {
        if (currentReport != null) {
            // There was already a crash being reported, the crash screen might have
            // crashed. Report it normally instead.
            LOGGER.error("An uncaught exception occured while displaying the crash screen, making normal report instead", report.getCrashCause());
            displayCrashReport(report);
            return;
        }

        currentReport = report;

        File crashReportsDir = new File(Minecraft.getMinecraft().mcDataDir, "crash-reports");
        File crashReportSaveFile = new File(crashReportsDir, "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-client.txt");

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
            Bootstrap.printToSYSOUT("Recoverable game crash! Crash report saved to: " + reportFile);
        } else {
            Bootstrap.printToSYSOUT("Recoverable game crash! Crash report could not be saved.");
        }

        // Reset hasCrashed and debugCrashKeyPressTime
        hasCrashed = false;
        debugCrashKeyPressTime = -1;

        // Display the crash screen
        displayGuiScreen(new GuiCrashScreen(reportFile, report));

        // Vanilla does this when switching to main menu but not our custom crash screen
        // nor the out of memory screen (see https://bugs.mojang.com/browse/MC-128953)
        gameSettings.showDebugInfo = false;
        ingameGUI.getChatGUI().clearChatMessages(true);
    }

    /**
     * @author Runemoro
     * @reason Disconnect from the current world and free memory, using a memory reserve
     * to make sure that an OutOfMemory doesn't happen while doing this.
     * <p>
     * Bugs Fixed:
     * - https://bugs.mojang.com/browse/MC-128953
     * - Memory reserve not recreated after out-of memory
     */
    @SuppressWarnings("CallToSystemGC")
    @Overwrite
    public void freeMemory() {
        try {
            memoryReserve = new byte[0];
            renderGlobal.deleteAllDisplayLists();
        } catch (Throwable ignored) {}

        try {
            System.gc();
            // Fix: Close the connection to avoid receiving packets from old server
            // when playing in another world (MC-128953)
            if (world != null) {
                world.sendQuittingDisconnectingPacket();
            }
            loadWorld(null);

            // Probably not necessary, but do this now rathe than next tick to make
            // it identical to a regular disconnect:
            if (entityRenderer.isShaderActive()) {
                entityRenderer.stopUseShader();
            }
        } catch (Throwable ignored) {}

        System.gc();

        // Fix: Re-create memory reserve so that future crashes work well too
        memoryReserve = new byte[10485760];
    }

    @Override
    public void clearCurrentReport() {
        currentReport = null;
    }
}
