package org.dimdev.vanillafix.blockstates.mixins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.MapPopulator;
import net.minecraft.util.math.Cartesian;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.dimdev.vanillafix.blockstates.IPatchedBlockStateContainer;
import org.dimdev.vanillafix.blockstates.NumericalBlockState;
import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;
import java.util.*;

@Mixin(BlockStateContainer.class)
public abstract class MixinBlockStateContainer implements IPatchedBlockStateContainer {
    @Shadow @Final @Mutable private Block block;
    @Shadow @Final @Mutable private ImmutableSortedMap<String, IProperty<?>> properties;
    @Shadow public static <T extends Comparable<T>> String validateProperty(Block block, IProperty<T> property) { return null; }
    @Shadow protected abstract List<Iterable<Comparable<?>>> getAllowedValues();

    private final ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties;
    private final Map<IProperty<?>, Integer> propertyOffsets = new HashMap<>();
    protected ImmutableList<IBlockState> validStatesCache;

    @Overwrite
    public MixinBlockStateContainer(Block block, IProperty<?>[] properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
        this.block = block;
        this.unlistedProperties = unlistedProperties;

        // Immutable map builder won't work, some mods have duplicate properties
        LinkedHashMap<String, IProperty<?>> propertyMap = new LinkedHashMap<>();
        int offset = 0;

        for (IProperty<?> property : properties) {
            validateProperty(block, property);
            propertyMap.put(property.getName(), property);

            NumericalBlockState.makePropertyInfo(property);
            propertyOffsets.put(property, offset);
            offset += MathHelper.log2(property.getAllowedValues().size()) + 1;
        }

        this.properties = ImmutableSortedMap.copyOf(propertyMap);
    }

    @Overwrite
    public ImmutableList<IBlockState> getValidStates() {
        if (validStatesCache == null) {
            ImmutableList.Builder<IBlockState> states = ImmutableList.builder();

            for (List<Comparable<?>> list : Cartesian.cartesianProduct(getAllowedValues())) {
                Map<IProperty<?>, Comparable<?>> propertyValueMap = MapPopulator.createMap(properties.values(), list);
                IBlockState state = createState(ImmutableMap.<IProperty<?>, Comparable<?>>builder().putAll(propertyValueMap).build(), unlistedProperties);
                states.add(state);
            }

            validStatesCache = states.build();
        }

        return validStatesCache;
    }

    @Overwrite(remap = false)
    protected BlockStateContainer.StateImplementation createState(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, @Nullable ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
        return null;
    }

    protected IBlockState createState(ImmutableMap<IProperty<?>, Comparable<?>> properties, @Nullable ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
        BlockStateContainer.StateImplementation state = createState(block, properties, unlistedProperties);
        if (state != null) {
            return state;
        } else {
            return NumericalBlockState.fromPropertyValueMap((BlockStateContainer) (Object) this, properties);
        }
    }

    @Overwrite
    public IBlockState getBaseState() {
        if (validStatesCache != null) {
            return validStatesCache.get(0);
        }

        if (unlistedProperties == null || unlistedProperties.isEmpty()) {
            return NumericalBlockState.get((BlockStateContainer) (Object) this, 0);
        }

        return getValidStates().get(0);
    }

    @Override
    public Map<IProperty<?>, Integer> getPropertyOffsets() {
        return propertyOffsets;
    }
}
