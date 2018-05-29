package org.dimdev.vanillafix.crashes;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.utils.HasteUpload;
import org.dimdev.vanillafix.ModConfig;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public abstract class GuiProblemScreen extends GuiScreen {
    private static final Logger log = LogManager.getLogger();

    protected final CrashReport report;
    private String hasteLink = null;
    private String modListString;

    public GuiProblemScreen(CrashReport report) {
        this.report = report;
    }

    @Override
    public void initGui() {
        mc.setIngameNotInFocus();
        buttonList.clear();
        buttonList.add(new GuiButton(1, width / 2 - 155 + 160, height / 4 + 120 + 12, 150, 20, I18n.format("vanillafix.gui.getLink")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            try {
                if (hasteLink == null) {
                    hasteLink = HasteUpload.uploadToHaste(ModConfig.crashes.hasteURL, "mccrash", report.getCompleteReport());
                }
                ReflectionHelper.findField(GuiScreen.class, "clickedLinkURI", "field_175286_t").set(this, new URI(hasteLink));
                mc.displayGuiScreen(new GuiConfirmOpenLink(this, hasteLink, 31102009, false));
            } catch (Throwable e) {
                log.error("Exception when crash menu button clicked:", e);
                button.displayString = I18n.format("vanillafix.gui.failed");
                button.enabled = false;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {}

    protected String getModListString() {
        if (modListString == null) {
            final Set<ModContainer> suspectedMods = ((IPatchedCrashReport) report).getSuspectedMods();
            if (suspectedMods == null) {
                return modListString = I18n.format("vanillafix.crashscreen.identificationErrored");
            }
            List<String> modNames = new ArrayList<>();
            for (ModContainer mod : suspectedMods) {
                modNames.add(mod.getName());
            }
            if (modNames.isEmpty()) {
                modListString = I18n.format("vanillafix.crashscreen.unknownCause");
            } else {
                modListString = StringUtils.join(modNames, ", ");
            }
        }
        return modListString;
    }
}
