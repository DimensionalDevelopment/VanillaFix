package org.dimdev.vanillafix;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigManager;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Jankson;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.impl.SyntaxError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.vanillafix.util.config.ModConfig;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class VanillaFix implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final ModContainer MOD = FabricLoader.getInstance().getModContainer("vanillafix").orElseThrow(IllegalStateException::new);
    private static ConfigManager<ModConfig> CONFIG_MANAGER;
    private static ModConfig MOD_CONFIG;
    public static final ModConfig CONFIG;
    public static final Jankson JANKSON = Jankson.builder().build();

    @Override
    public void onInitialize() {
        CONFIG_MANAGER = (ConfigManager<ModConfig>) AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        MOD_CONFIG = CONFIG_MANAGER.getConfig();
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

    public static ModConfig config() {
        return MOD_CONFIG;
    }

    public static void save() {
        CONFIG_MANAGER.save();
    }

    static {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("vanillafix.json5");
        if (!Files.exists(configPath)) {
            CONFIG = new ModConfig();
        } else {
            try (BufferedReader reader = Files.newBufferedReader(configPath)) {
                StringBuilder jsonBuilder = new StringBuilder();
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    jsonBuilder.append(line);
                }
                String json = jsonBuilder.toString();
                CONFIG = JANKSON.fromJson(json, ModConfig.class);
            } catch (IOException | SyntaxError e) {
                throw new RuntimeException(e);
            }
        }
    }
}
