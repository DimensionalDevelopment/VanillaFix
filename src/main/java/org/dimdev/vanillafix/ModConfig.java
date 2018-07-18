package org.dimdev.vanillafix;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraftforge.common.config.Config.*;

@Config(modid = "vanillafix", name = "vanillafix", category = "")
public final class ModConfig {

    public enum ProblemAction {
        @LangKey("vanillafix.crashes.problemAction.log") LOG,
        @LangKey("vanillafix.crashes.problemAction.notification") NOTIFICATION,
        @LangKey("vanillafix.crashes.problemAction.warningScreen") WARNING_SCREEN,
        @LangKey("vanillafix.crashes.problemAction.crash") CRASH
    }

    public static Fixes fixes = new Fixes();
    public static Crashes crashes = new Crashes();

    public static class Fixes {
        @Name("bugFixes")
        @LangKey("vanillafix.fixes.bugFixes")
        @RequiresMcRestart
        public boolean bugFixes = true;

        @Name("crashFixes")
        @LangKey("vanillafix.fixes.crashFixes")
        @RequiresMcRestart
        public boolean crashFixes = true;

        @Name("modSupport")
        @LangKey("vanillafix.fixes.modSupport")
        @RequiresMcRestart
        public boolean modSupport = true;

        @Name("profiler")
        @LangKey("vanillafix.fixes.profiler")
        @RequiresMcRestart
        public boolean profiler = true;

        @Name("textureFixes")
        @LangKey("vanillafix.fixes.textureFixes")
        @RequiresMcRestart
        public boolean textureFixes = true;

        @Name("blockstates")
        @LangKey("vanillafix.fixes.blockstates")
        @RequiresMcRestart
        public boolean blockstates = true;

        @Name("dynamicresources")
        @LangKey("vanillafix.fixes.dynamicresources")
        @RequiresMcRestart
        public boolean dynamicresources = true;
    }

    public static class Crashes {
        @Name("disableReturnToMainMenu")
        @LangKey("vanillafix.crashes.disableReturnToMainMenu")
        public boolean disableReturnToMainMenu = false;

        @Name("scheduledTaskproblemAction")
        @LangKey("vanillafix.crashes.scheduledTaskproblemAction")
        public ProblemAction scheduledTaskAction = ProblemAction.NOTIFICATION;

        @Name("hasteURL")
        @LangKey("vanillafix.crashes.hasteURL")
        public String hasteURL = "https://paste.dimdev.org";

        @Name("replaceErrorNotifications")
        @LangKey("vanillafix.crashes.replaceErrorNotifications")
        public boolean replaceErrorNotifications = false;

        @Name("errorNotificationDuration")
        @LangKey("vanillafix.crashes.errorNotificationDuration")
        public int errorNotificationDuration = 30000;
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals("vanillafix")) {
            ConfigManager.sync(event.getModID(), Type.INSTANCE);
        }
    }
}