package org.dimdev.vanillafix;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigManager;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.vanillafix.util.config.ModConfig;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class VanillaFix implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final ModContainer MOD = FabricLoader.getInstance().getModContainer("vanillafix").orElseThrow(IllegalStateException::new);
    private static final ConfigManager<ModConfig> CONFIG_MANAGER;
    private static final ModConfig MOD_CONFIG;

    @Override
    public void onInitialize() {
        if (MOD.getMetadata().getVersion().getFriendlyString().contains("beta")) {
            LOGGER.warn("================================================");
            LOGGER.warn("You are running a beta version of VanillaFix!");
            LOGGER.warn("VanillaFix Version: {}", MOD.getMetadata().getVersion().getFriendlyString());
            LOGGER.warn("If you find any incompatibilities or unexpected");
            LOGGER.warn("behavior, please create a github issue to let us");
            LOGGER.warn("know about the problem.");
            LOGGER.warn("================================================");
        }
    }

    static {
        CONFIG_MANAGER = (ConfigManager<ModConfig>) AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        MOD_CONFIG = CONFIG_MANAGER.getConfig();
    }

    public static ModConfig config() {
        return MOD_CONFIG;
    }

    public static void save() {
        CONFIG_MANAGER.save();
    }
}
