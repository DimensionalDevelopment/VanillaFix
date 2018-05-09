package org.dimdev.vanillafix;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraftforge.common.config.Config.*;

@Config(modid = "vanillafix", name = "vanillafix", category = "")
public final class ModConfig {
    public enum ProblemAction {
        LOG,
        WARNING_SCREEN,
        CRASH
    }

    public static Fixes fixes = new Fixes();
    public static Crashes crashes = new Crashes();

    public int configVersion = 0;

    public static class Fixes {
        // TODO
    }

    public static class Crashes {
        @Name("scheduledTaskExceptionAction")
        @LangKey("vanillafix.crashes.scheduledTaskExceptionAction")
        public ProblemAction scheduledTaskAction = ProblemAction.WARNING_SCREEN;

        @Name("hasteURL")
        @LangKey("vanillafix.crashes.hasteURL")
        public String hasteURL = "https://paste.dimdev.org";
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals("vanillafix")) {
            ConfigManager.sync(event.getModID(), Type.INSTANCE);
        }
    }
}