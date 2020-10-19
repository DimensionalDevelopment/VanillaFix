package org.dimdev.vanillafix;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.impl.SyntaxError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.vanillafix.util.config.ModConfig;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class VanillaFix implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final ModContainer MOD = FabricLoader.getInstance().getModContainer("vanillafix").orElseThrow(IllegalStateException::new);
    private static ModConfig MOD_CONFIG;
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("vanillafix.json5");
    public static final Jankson JANKSON = Jankson.builder().build();

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
        try {
            if (Files.exists(PATH)) {
                MOD_CONFIG = JANKSON.fromJson(JANKSON.load(Files.newInputStream(PATH)), ModConfig.class);
            } else {
                Files.createFile(PATH);
                Files.write(PATH, JANKSON.toJson(MOD_CONFIG).toJson(true, true).getBytes());
            }
        } catch (IOException e) {
            LOGGER.error("Error loading config. Using default values");
            e.printStackTrace();
            MOD_CONFIG = new ModConfig();
        } catch (SyntaxError e) {
            LOGGER.error("Caught a Syntax error when loading config. Using default values");
            LOGGER.error(e.getCompleteMessage());
            e.printStackTrace();
            MOD_CONFIG = new ModConfig();
        }
    }

    public static void save() {
        String json = JANKSON.toJson(MOD_CONFIG).toJson(true, true);
        try {
            Files.write(PATH, json.getBytes());
        } catch (IOException e) {
            LOGGER.error("Error saving config");
            e.printStackTrace();
        }
    }

    public static ModConfig config() {
        return MOD_CONFIG;
    }
}
