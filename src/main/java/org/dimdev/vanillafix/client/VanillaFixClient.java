package org.dimdev.vanillafix.client;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;

import org.dimdev.vanillafix.util.config.ModConfig;

import net.fabricmc.api.ClientModInitializer;

public class VanillaFixClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AutoConfig.getGuiRegistry(ModConfig.class);
    }
}
