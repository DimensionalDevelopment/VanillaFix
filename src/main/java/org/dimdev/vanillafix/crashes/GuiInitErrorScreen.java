package org.dimdev.vanillafix.crashes;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiInitErrorScreen extends GuiProblemScreen {

    public GuiInitErrorScreen(CrashReport report) {
        super(report);
    }

    @Override
    public void initGui() {
        mc.setIngameNotInFocus();
        buttonList.clear();
        buttonList.add(new GuiButton(1, width / 2 - 155,  height / 4 + 120 + 12, 310, 20, I18n.format("vanillafix.gui.getLink")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) { // TODO: localize number of lines
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("vanillafix.initerrorscreen.title"), width / 2, height / 4 - 40, 0xFFFFFF);

        int textColor = 0xD0D0D0;
        int x = width / 2 - 155;
        int y = height / 4;

        drawString(fontRenderer, I18n.format("vanillafix.initerrorscreen.summary"), x, y, textColor);
        drawString(fontRenderer, I18n.format("vanillafix.crashscreen.paragraph1.line1"), x, y += 18, textColor);

        drawCenteredString(fontRenderer, getModListString(), width / 2, y += 11, 0xE0E000);

        drawString(fontRenderer, I18n.format("vanillafix.crashscreen.paragraph2.line1"), x, y += 11, textColor);
        drawString(fontRenderer, I18n.format("vanillafix.crashscreen.paragraph2.line2"), x, y += 9, textColor);

        drawCenteredString(fontRenderer, report.getFile() != null ? "\u00A7n" + report.getFile().getName() : I18n.format("vanillafix.crashscreen.reportSaveFailed"), width / 2, y += 11, 0x00FF00);

        drawString(fontRenderer, I18n.format("vanillafix.initerrorscreen.paragraph3.line1"), x, y += 12, textColor);
        drawString(fontRenderer, I18n.format("vanillafix.initerrorscreen.paragraph3.line2"), x, y += 9, textColor);
        drawString(fontRenderer, I18n.format("vanillafix.initerrorscreen.paragraph3.line3"), x, y += 9, textColor);
        drawString(fontRenderer, I18n.format("vanillafix.initerrorscreen.paragraph3.line4"), x, y + 9, textColor);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
