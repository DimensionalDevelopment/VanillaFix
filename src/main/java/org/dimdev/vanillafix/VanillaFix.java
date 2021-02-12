package org.dimdev.vanillafix;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigHolder;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Jankson;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.JsonObject;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.impl.SyntaxError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.vanillafix.util.ConfigPair;
import org.dimdev.vanillafix.util.config.ModConfig;
import org.dimdev.vanillafix.util.serialization.JanksonOps;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class VanillaFix implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final ModContainer MOD = FabricLoader.getInstance().getModContainer("vanillafix").orElseThrow(IllegalStateException::new);
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("vanillafix.json5");
	public static final Jankson JANKSON = Jankson.builder().build();
	public static final Map<String, ConfigPair> MIXIN_CONFIGS;
	public static final ConfigHolder<ModConfig> CONFIG = AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);

	@Override
	public void onInitialize() {
		if (MOD.getMetadata().getVersion().getFriendlyString().contains("beta")) {
			LOGGER.warn("================================================");
			LOGGER.warn("You are running a beta version of VanillaFix!");
			LOGGER.warn("VanillaFix Version: {}", MOD.getMetadata().getVersion().getFriendlyString());
			LOGGER.warn("We can not guarantee that this version is compatible");
			LOGGER.warn("with all mods. Please report issues to discord if found.");
			LOGGER.warn("================================================");
		}
	}

	static {
		ImmutableMap.Builder<String, ConfigPair> builder = ImmutableMap.builder();
		try (InputStream stream = VanillaFix.class.getResourceAsStream("/data/vanillafix/mixin_configs.json")) {
			JsonObject jsonObject = JANKSON.load(stream);
			//noinspection UnstableApiUsage
			builder.putAll(jsonObject.entrySet()
					.stream()
					.filter(entry -> entry.getValue() instanceof JsonObject)
					.map(entry -> new Map.Entry<String, ConfigPair>() {
						@Override
						public String getKey() {
							return entry.getKey();
						}

						@Override
						public ConfigPair getValue() {
							return ConfigPair.CODEC.parse(JanksonOps.INSTANCE, entry.getValue()).getOrThrow(false, System.err::println);
						}

						@Override
						public ConfigPair setValue(ConfigPair value) {
							throw new UnsupportedOperationException();
						}
					}).collect(Collectors.toSet()));
		} catch (IOException | SyntaxError e) {
			throw new AssertionError(e);
		}
		MIXIN_CONFIGS = builder.build();
	}

	public static ModConfig config() {
		return CONFIG.get();
	}
}
