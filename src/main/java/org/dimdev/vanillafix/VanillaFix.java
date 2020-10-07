package org.dimdev.vanillafix;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigManager;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.vanillafix.util.config.ModConfig;

import net.fabricmc.api.ModInitializer;

public class VanillaFix implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static ConfigManager<ModConfig> configManager;
    public static ModConfig modConfig;

    @Override
    public void onInitialize() {

    }

    static {
        configManager = (ConfigManager<ModConfig>) AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        modConfig = configManager.getConfig();
    }
}
