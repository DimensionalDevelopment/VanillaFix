package org.dimdev.vanillafix.bugs.mixins;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

/**
 * Removes the propertyValueTable to improve memory usage and init time, but in exchange
 * makes withProperty slightly slower.
 */
@Mixin(BlockStateContainer.StateImplementation.class)
public abstract class MixinStateImplementation {
    @Shadow @Final private ImmutableMap<IProperty<?>, Comparable<?>> properties;
    @Shadow protected abstract Map<IProperty<?>, Comparable<?>> getPropertiesWithValue(IProperty<?> property, Comparable<?> value);
    @Shadow @Final private Block block;
    private Map<Map<IProperty<?>, Comparable<?>>, BlockStateContainer.StateImplementation> propertyValueMap;

    /**
     * @reason Disable the propertyValueTable, and just store the map instead.
     */
    @Overwrite
    public void buildPropertyValueTable(Map<Map<IProperty<?>, Comparable<?>>, BlockStateContainer.StateImplementation> map) {
        propertyValueMap = map;
    }

    /**
     * @reason Get the IBlockState from the map rather than the table.
     */
    @Overwrite
    public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
        Comparable<?> comparable = properties.get(property);

        if (comparable == null) {
            throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + block.getBlockState());
        } else if (comparable == value) {
            return (BlockStateContainer.StateImplementation) (Object) this;
        } else {
            IBlockState state = propertyValueMap.get(getPropertiesWithValue(property, value));

            if (state == null) {
                throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on block " + Block.REGISTRY.getNameForObject(block) + ", it is not an allowed value");
            } else {
                return state;
            }
        }
    }
}
