package org.dimdev.vanillafix.idlimit.mixins;

import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Removes ID limits from forge registries. These are @ModifyConstants since javac
 * automatically inlines all static final fields that are constant expressions
 * (https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.28)
 */
@Mixin(GameData.class)
public abstract class MixinGameData {
    /** @reason Removes the block ID limit. */
    @ModifyConstant(method = "init", constant = @Constant(intValue = 4095, ordinal = 0), remap = false)
    private static int getBlockIDLimit(int value) {
        return Integer.MAX_VALUE - 1;
    }

    /** @reason Removes the item ID limit. */
    @ModifyConstant(method = "init", constant = @Constant(intValue = 31999, ordinal = 0), remap = false)
    private static int getItemIDLimit(int value) {
        return Integer.MAX_VALUE - 1;
    }
}
