package org.dimdev.vanillafix;

import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.utils.HasteUpload;
import org.dimdev.utils.SSLUtils;

import java.io.File;
import java.net.URI;

@SideOnly(Side.CLIENT)
public class GuiCrashScreen extends GuiScreen {
    private static final String HASTE_BASE_URL = "https://paste.dimdev.org";
    private static final Logger log = LogManager.getLogger();
    private static boolean patchedSSL = false;

    private File reportFile;
    private final CrashReport report;
    private String hasteLink = null;

    public GuiCrashScreen(File reportFile, CrashReport report) {
        this.reportFile = reportFile;
        this.report = report;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiOptionButton(0, width / 2 - 155, height / 4 + 120 + 12, I18n.format("gui.toTitle")));
        buttonList.add(new GuiOptionButton(1, width / 2 - 155 + 160, height / 4 + 120 + 12, I18n.format("vanillafix.gui.getLink")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        try {
            if (button.id == 0) {
                mc.displayGuiScreen(new GuiMainMenu());
                ((IPatchedMinecraft) mc).clearCurrentReport();
            } else if (button.id == 1) {
                if (hasteLink == null) {
                    // This is just a quick fix for now. Instead, we should use a TrustManager that wraps the
                    // default one and trusts IdenTrust, and eventually removed when Minecraft updates to a
                    // version of Java that trusts IdenTrust (root certificate for Let's Encrypt).
                    // See: https://stackoverflow.com/questions/34110426/does-java-support-lets-encrypt-certificates
                    if (!patchedSSL) {
                        // TODO: Remove this as soon as possible, or at least restore old TrustManager after upload
                        SSLUtils.trustAllCertificates();
                        patchedSSL = true;
                    }

                    hasteLink = HasteUpload.uploadToHaste(HASTE_BASE_URL, "txt", report.getCompleteReport());
                }
                ReflectionHelper.findField(GuiScreen.class, "clickedLinkURI", "field_175286_t").set(this, new URI(hasteLink));
                mc.displayGuiScreen(new GuiConfirmOpenLink(this, hasteLink, 31102009, false));
            }
        } catch (Throwable e) {
            log.error("Exception when crash menu button clicked:", e);
            button.displayString = I18n.format("vanillafix.gui.failed");
            button.enabled = false;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, "Minecraft crashed!", width / 2, height / 4 - 40, 0xFFFFFF);
        drawString(fontRenderer, "Minecraft ran into a problem and crashed.", width / 2 - 160, height / 4, 0xA0A0A0);
        drawString(fontRenderer, "This is probably caused by a bug in one of your mods or vanilla", width / 2 - 160, height / 4 + 18, 0xA0A0A0);
        drawString(fontRenderer, "Minecraft. Since you have VanillaFix installed, you can return to", width / 2 - 160, height / 4 + 27, 0xA0A0A0);
        drawString(fontRenderer, "the main menu and keep playing despite the crash.", width / 2 - 160, height / 4 + 36, 0xA0A0A0);
        drawString(fontRenderer, "A crash report has been generated, and can be found here (click):", width / 2 - 160, height / 4 + 54, 0xA0A0A0);
        drawCenteredString(fontRenderer, reportFile != null ? "\u00A7n" + reportFile.getName() : "(report failed to save, see the log instead)", width / 2, height / 4 + 65, 0x00FF00);
        drawString(fontRenderer, "You are encouraged to send it to the mod's author to fix this issue", width / 2 - 160, height / 4 + 78, 0xA0A0A0);
        drawString(fontRenderer, "Click the \"Get link\" button to view the crash report, which contains", width / 2 - 160, height / 4 + 87, 0xA0A0A0);
        drawString(fontRenderer, "more info such as which mod(s) caused the crash.", width / 2 - 160, height / 4 + 96, 0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
