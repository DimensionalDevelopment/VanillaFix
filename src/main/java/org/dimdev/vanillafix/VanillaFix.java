package org.dimdev.vanillafix;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.crash.CrashReport;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

@Mod(modid = "vanillafix",
     name = "VanillaFix",
     version = "${version}",
     acceptableRemoteVersions = "*",
     updateJSON = "https://gist.githubusercontent.com/Runemoro/28e8cf4c24a5f17f508a5d34f66d229f/raw/vanillafix_update.json")
public class VanillaFix {
    private static final int CONFIG_VERSION = 1;
    private static final boolean DEBUG_BLOCK_IDS = false;
    private static final boolean DEBUG_ITEM_IDS = false;
    private static final boolean DEBUG_INIT_ERROR = false; // For testing the init error screen outside of dev. Don't forget to unset!

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        Logger log = event.getModLog();
        File modDir = new File(event.getModConfigurationDirectory(), "vanillafix");
        modDir.mkdirs();

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

        IForgeRegistry<Block> blockRegistry = GameRegistry.findRegistry(Block.class);
        IForgeRegistry<Item> itemRegistry = GameRegistry.findRegistry(Item.class);

        if (DEBUG_BLOCK_IDS) {
            for (int i = 0; i < 5000; i++) {
                Block block = new Block(Material.GROUND)
                        .setCreativeTab(CreativeTabs.BUILDING_BLOCKS)
                        .setUnlocalizedName("block_" + i)
                        .setRegistryName(new ResourceLocation("vanillafix:block_" + i));

                blockRegistry.register(block);
                itemRegistry.register(new ItemBlock(block).setRegistryName(new ResourceLocation("vanillafix:block_" + i)));
            }
        }

        if (DEBUG_ITEM_IDS) {
            for (int i = 0; i < 40000; i++) {
                Item item = new Item()
                        .setCreativeTab(CreativeTabs.FOOD)
                        .setUnlocalizedName("item_" + i)
                        .setRegistryName(new ResourceLocation("vanillafix:item_" + i));

                itemRegistry.register(item);
            }
        }

        if (DEBUG_INIT_ERROR) throw new ReportedException(new CrashReport("Debug init crash", new Throwable()));
    }
}
