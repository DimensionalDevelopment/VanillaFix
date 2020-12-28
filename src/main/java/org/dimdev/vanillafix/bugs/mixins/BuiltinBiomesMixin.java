package org.dimdev.vanillafix.bugs.mixins;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.dimdev.vanillafix.util.annotation.MixinConfigValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.biome.DefaultBiomeCreator;

@MixinConfigValue(category = "bugFixes", value = "fixStoneShoreColors")
@Mixin(BuiltinBiomes.class)
public class BuiltinBiomesMixin {
	@Shadow
	@Final
	private static Int2ObjectMap<RegistryKey<Biome>> BY_RAW_ID;

	/**
	 * Fixes a bug where stone shores, despite being a cold biome
	 * do not have the cold water color
	 * Bug Fixed
	 * - https://bugs.mojang.com/browse/MC-200634
	 */
	@Inject(method = "register", at = @At("HEAD"), cancellable = true)
	private static void interceptRegister(int rawId, RegistryKey<Biome> registryKey, Biome biome, CallbackInfoReturnable<Biome> cir) {
		if (registryKey.equals(BiomeKeys.STONE_SHORE)) {
			cir.setReturnValue(registerUnsafely(rawId, registryKey, DefaultBiomeCreator.createBeach(0.1F, 0.8F, 0.2F, 0.3F, 0x3d57d6, false, true)));
		}
	}

	@Unique
	private static Biome registerUnsafely(int rawId, RegistryKey<Biome> registryKey, Biome biome) {
		BY_RAW_ID.put(rawId, registryKey);
		return BuiltinRegistries.set(BuiltinRegistries.BIOME, rawId, registryKey, biome);
	}
}
