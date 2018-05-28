package org.dimdev.vanillafix;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import org.dimdev.vanillafix.crashes.StacktraceDeobfuscator;

import java.io.*;
import java.util.Scanner;

@Mod(modid = "vanillafix",
     name = "VanillaFix",
     version = "${version}",
     acceptableRemoteVersions = "*",
     updateJSON = "https://gist.githubusercontent.com/Runemoro/28e8cf4c24a5f17f508a5d34f66d229f/raw/vanillafix_update.json")
public class VanillaFix {
    private static final int CONFIG_VERSION = 1;
    String MCP_VERSION = "20180519-1.12";
    public File modDir;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        Logger log = event.getModLog();
        modDir = new File(event.getModConfigurationDirectory(), "vanillafix");
        modDir.mkdirs();

        // Initialize StacktraceDeobfuscator
        // TODO: Use version for current Minecraft version!
        log.info("Initializing StacktraceDeobfuscator");
        try {
            File mappings = new File(modDir, "methods-" + MCP_VERSION + ".csv");
            if (mappings.exists()) {
                log.info("Found MCP method mappings: " + mappings.getName());
            } else {
                log.info("Downloading MCP method mappings to: " + mappings.getName());
            }
            StacktraceDeobfuscator.init(mappings, MCP_VERSION);
        } catch (Exception e) {
            log.error("Failed to get MCP data!", e);
        }
        log.info("Done initializing StacktraceDeobfuscator");

        // Check if config is outdated and needs to be deleted
        boolean configOutdated;
        File configVersionFile = new File(modDir, "config_version");
        if (configVersionFile.exists()) {
            try (FileReader reader = new FileReader(configVersionFile); Scanner scanner = new Scanner(reader)) {
                try {
                    configOutdated = scanner.nextInt() != CONFIG_VERSION;
                } catch (NumberFormatException e) {
                    configOutdated = true;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            configOutdated = true;
        }

        if (configOutdated) {
            File configFile = new File(event.getModConfigurationDirectory(), "vanillafix.cfg");
            if (configFile.exists()) {
                log.info("Regenerating outdated config");
                configFile.delete();
            }
            try (FileWriter writer = new FileWriter(configVersionFile)) {
                writer.write(String.valueOf(CONFIG_VERSION));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        ConfigManager.sync("vanillafix", Config.Type.INSTANCE);

        // Register event listeners
        MinecraftForge.EVENT_BUS.register(ModConfig.class);
    }
}
