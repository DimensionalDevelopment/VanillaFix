package org.dimdev.vanillafix.blockstates.mixins;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PropertyHelper.class)
public abstract class MixinPropertyHelper<T extends Comparable<T>> implements IProperty<T> {
    /**
     * Fix mods using duplicate property names.
     */
    @Inject(method = "equals", at = @At("RETURN"), cancellable = true)
    public void overrideEquals(Object obj, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && obj instanceof PropertyHelper) {
            if (!getClass().getName().startsWith("net.minecraft") && !getAllowedValues().equals(((PropertyHelper<?>) obj).getAllowedValues())) {
                cir.setReturnValue(false);
            }
        }
    }
}
