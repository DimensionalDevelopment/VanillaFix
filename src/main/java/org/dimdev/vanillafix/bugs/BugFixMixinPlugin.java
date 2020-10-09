package org.dimdev.vanillafix.bugs;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Jankson;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.impl.SyntaxError;
import org.dimdev.vanillafix.util.config.ModConfig;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

public class BugFixMixinPlugin implements IMixinConfigPlugin {
    private static ModConfig CONFIG;
    private static final Jankson JANKSON = Jankson.builder().build();

    @Override
    public void onLoad(String mixinPackage) {
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

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    // TODO: make this better
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        switch (mixinClassName) {
            case "org.dimdev.vanillafix.bugs.mixins.step.ServerPlayerEntityMixin":
                return CONFIG.antiCheat.fixStepHeight;
            case "org.dimdev.vanillafix.bugs.mixins.EntityMixin":
                return CONFIG.bugFixes.updateFallDistance;
            case "org.dimdev.vanillafix.bugs.mixins.MinecraftServerMixin":
                return CONFIG.bugFixes.disableInitialChunkLoad;
            case "org.dimdev.vanillafix.bugs.mixins.PlayerInventoryMixin":
                return CONFIG.bugFixes.fixRecipeBookIngredientsWithTags;
            case "org.dimdev.vanillafix.bugs.mixins.invulnerable.ServerPlayerEntityMixin":
                return CONFIG.antiCheat.noPlayerInvulnerabilityAfterTeleport;
            case "org.dimdev.vanillafix.bugs.mixins.client.MinecraftClientMixin":
                return CONFIG.clientOnly.splitScreenAndTextureProfiler;
            case "org.dimdev.vanillafix.bugs.mixins.client.ClientPlayerEntityMixin":
                return CONFIG.clientOnly.screenInNetherPortal;
            case "org.dimdev.vanillafix.bugs.mixins.client.ClientPlayNetworkHandlerMixin":
                return CONFIG.clientOnly.fastInterdimensionalTeleportation;
            case "org.dimdev.vanillafix.bugs.mixins.BuiltinBiomesMixin":
                return CONFIG.bugFixes.fixStoneShoreColors;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
