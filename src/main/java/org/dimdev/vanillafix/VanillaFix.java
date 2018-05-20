package org.dimdev.vanillafix;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import org.dimdev.vanillafix.crashes.StacktraceDeobfuscator;

import java.io.File;

@Mod(modid = "vanillafix", name = "VanillaFix", version = "${version}", acceptableRemoteVersions = "*")
public class VanillaFix {
    String MCP_VERSION = "20180519-1.12";

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        Logger log = event.getModLog();
        File configDir = event.getModConfigurationDirectory();
        configDir.mkdirs();

        // TODO: Use version for current Minecraft version!
        log.info("Initializing StacktraceDeobfuscator");
        try {
            File mappings = new File(configDir, "methods-" + MCP_VERSION + ".csv");
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
    }
}
