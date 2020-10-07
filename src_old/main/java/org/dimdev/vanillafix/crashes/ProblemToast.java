package org.dimdev.vanillafix.crashes;

import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.fml.common.ModContainer;
import org.dimdev.vanillafix.ModConfig;

import java.util.Set;

public class ProblemToast implements IToast {
    public final CrashReport report;
    private String suspectedModString;
    public boolean hide;

    public ProblemToast(CrashReport report) {
        this.report = report;
    }

    @Override
    public IToast.Visibility draw(GuiToast toastGui, long delta) {
        if (hide) return Visibility.HIDE;
        
        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        toastGui.drawTexturedModalRect(0, 0, 0, 96, 160, 32);

        toastGui.getMinecraft().fontRenderer.drawString(getModCause().equals("")
                ? I18n.format("vanillafix.notification.title.unknown")
                : I18n.format("vanillafix.notification.title.mod", getModCause()), 5, 7, 0xff000000);
        toastGui.getMinecraft().fontRenderer.drawString(I18n.format("vanillafix.notification.description"), 5, 18, 0xff500050);

        return delta >= ModConfig.crashes.errorNotificationDuration ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
    }

    private Object getModCause() {
        if (suspectedModString == null) {
            Set<ModContainer> suspectedMods = ((IPatchedCrashReport) report).getSuspectedMods();
            suspectedModString = suspectedMods.isEmpty() ? "" : suspectedMods.iterator().next().getName();
        }
        return suspectedModString;
    }
}
