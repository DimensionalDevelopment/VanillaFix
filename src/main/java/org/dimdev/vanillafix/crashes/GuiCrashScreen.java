package org.dimdev.vanillafix.crashes;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dimdev.vanillafix.ModConfig;

@SideOnly(Side.CLIENT)
public class GuiCrashScreen extends GuiProblemScreen {

    public GuiCrashScreen(CrashReport report) {
        super(report);
    }

    @Override
    public void initGui() {
        super.initGui();
        GuiOptionButton mainMenuButton = new GuiOptionButton(0, width / 2 - 155, height / 4 + 120 + 12, I18n.format("gui.toTitle"));
        buttonList.add(mainMenuButton);
        if (ModConfig.crashes.disableReturnToMainMenu) {
            mainMenuButton.enabled = false;
            mainMenuButton.displayString = I18n.format("vanillafix.gui.disabledByConfig");
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        if (button.id == 0) {
            mc.displayGuiScreen(new GuiMainMenu());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) { // TODO: localize number of lines
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("vanillafix.crashscreen.title"), width / 2, height / 4 - 40, 0xFFFFFF);

        int textColor = 0xD0D0D0;
        int x = width / 2 - 155;
        int y = height / 4;

        drawString(fontRenderer, I18n.format("vanillafix.crashscreen.summary"), x, y, textColor);
        drawString(fontRenderer, I18n.format("vanillafix.crashscreen.paragraph1.line1"), x, y += 18, textColor);

        drawCenteredString(fontRenderer, getModListString(), width / 2, y += 11, 0xE0E000);

        drawString(fontRenderer, I18n.format("vanillafix.crashscreen.paragraph2.line1"), x, y += 11, textColor);
        drawString(fontRenderer, I18n.format("vanillafix.crashscreen.paragraph2.line2"), x, y += 9, textColor);

        drawCenteredString(fontRenderer, report.getFile() != null ? "\u00A7n" + report.getFile().getName() : I18n.format("vanillafix.crashscreen.reportSaveFailed"), width / 2, y += 11, 0x00FF00);

        drawString(fontRenderer, I18n.format("vanillafix.crashscreen.paragraph3.line1"), x, y += 12, textColor);
        drawString(fontRenderer, I18n.format("vanillafix.crashscreen.paragraph3.line2"), x, y += 9, textColor);
        drawString(fontRenderer, I18n.format("vanillafix.crashscreen.paragraph3.line3"), x, y += 9, textColor);
        drawString(fontRenderer, I18n.format("vanillafix.crashscreen.paragraph3.line4"), x, y + 9, textColor);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
