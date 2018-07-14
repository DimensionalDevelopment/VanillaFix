package org.dimdev.vanillafix.blockstates.mixins;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PropertyHelper.class)
public abstract class MixinPropertyHelper implements IProperty {
    @Shadow @Final private Class<?> valueClass;
    @Shadow @Final private String name;

    /** @reason Fix for mods using the same name and class for two properties. */
    @Overwrite
    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PropertyHelper)) {
            return false;
        } else {
            PropertyHelper<?> property = (PropertyHelper) obj;
            return valueClass.equals(property.getValueClass())
                   && name.equals(property.getName())
                   && getAllowedValues().equals(property.getAllowedValues());
        }
    }
}
