package org.dimdev.vanillafix.blockstates.mixins;

import com.google.common.collect.*;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.MapPopulator;
import net.minecraft.util.math.Cartesian;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.dimdev.vanillafix.blockstates.IPatchedBlockStateContainer;
import org.dimdev.vanillafix.blockstates.NumericalBlockState;
import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstructorNotProtectedInAbstractClass"})
@Mixin(BlockStateContainer.class)
public abstract class MixinBlockStateContainer implements IPatchedBlockStateContainer {
    // @formatter:off
    @Shadow @Final @Mutable private Block block;
    @Shadow @Final @Mutable private ImmutableSortedMap<String, IProperty<?>> properties;
    @Shadow public static <T extends Comparable<T>> String validateProperty(Block block, IProperty<T> property) { return null; }
    @Shadow protected abstract List<Iterable<Comparable<?>>> getAllowedValues();
    @Shadow public abstract Block getBlock();
    @Shadow @Final private ImmutableList<IBlockState> validStates;
    // @formatter:on

    private final ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties;
    private final Map<IProperty<?>, Integer> propertyOffsets = new HashMap<>();
    protected ImmutableList<IBlockState> validStatesCache = null;
    @SuppressWarnings({"FieldCanBeLocal", "unused"}) private final Object x; // workaround for mixin bug

    @Overwrite
    public MixinBlockStateContainer(Block block, IProperty<?>[] properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
        this.block = block;
        this.unlistedProperties = unlistedProperties;

        // Immutable map builder won't work, some mods have duplicate properties
        Map<String, IProperty<?>> propertyMap = new LinkedHashMap<>();
        int offset = 0;

        for (IProperty<?> property : properties) {
            validateProperty(block, property);
            propertyMap.put(property.getName(), property);

            if (vanillafix$isNumerical()) {
                NumericalBlockState.makePropertyInfo(property);
                propertyOffsets.put(property, offset);
                offset += MathHelper.log2(property.getAllowedValues().size()) + 1;
            }
        }

        this.properties = ImmutableSortedMap.copyOf(propertyMap);

        if (!vanillafix$isNumerical()) {
            Map<Map<IProperty<?>, Comparable<?>>, BlockStateContainer.StateImplementation> map2 = Maps.newLinkedHashMap();
            List<BlockStateContainer.StateImplementation> validStates = Lists.newArrayList();

            for (List<Comparable<?>> list : Cartesian.cartesianProduct(getAllowedValues())) {
                Map<IProperty<?>, Comparable<?>> map1 = MapPopulator.createMap(this.properties.values(), list);
                BlockStateContainer.StateImplementation blockstatecontainer$stateimplementation = createState(block, ImmutableMap.copyOf(map1), unlistedProperties);
                map2.put(map1, blockstatecontainer$stateimplementation);
                validStates.add(blockstatecontainer$stateimplementation);
            }

            for (BlockStateContainer.StateImplementation blockstatecontainer$stateimplementation1 : validStates) {
                blockstatecontainer$stateimplementation1.buildPropertyValueTable(map2);
            }

            this.validStates = ImmutableList.copyOf(validStates);
        }

        x = new Object();
    }

    @Overwrite
    public ImmutableList<IBlockState> getValidStates() {
        if (!vanillafix$isNumerical()) {
            return validStates;
        }

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
        if (!vanillafix$isNumerical()) {
            return new BlockStateContainer.StateImplementation(block, properties);
        }

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
        if (!vanillafix$isNumerical()) {
            return validStates.get(0);
        }

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

    @SuppressWarnings({"ConstantConditions", "EqualsBetweenInconvertibleTypes"})
    protected boolean vanillafix$isNumerical() {
        return getClass().equals(BlockStateContainer.class) || getClass().equals(ExtendedBlockState.class);
    }
}
