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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    protected ImmutableList<IBlockState> validStatesCache;
    private ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties;
    private Map<IProperty<?>, Integer> propertyOffsets;

    @Inject(
            method = "<init>(Lnet/minecraft/block/Block;[Lnet/minecraft/block/properties/IProperty;Lcom/google/common/collect/ImmutableMap;)V",
            at = @At(
                    value = "RETURN"
            )
    )
    private void onInit(Block block, IProperty<?>[] properties, ImmutableMap<net.minecraftforge.common.property.IUnlistedProperty<?>, java.util.Optional<?>> unlistedProperties, CallbackInfo callbackInfo) {
        this.unlistedProperties = unlistedProperties;
        this.propertyOffsets = new HashMap<>();

        int offset = 0;
        for (IProperty<?> property : properties) {
            if (vanillafix$isNumerical()) {
                NumericalBlockState.makePropertyInfo(property);
                this.propertyOffsets.put(property, offset);
                offset += MathHelper.log2(property.getAllowedValues().size()) + 1;
            }
        }
    }

    @Redirect(
            method = "<init>(Lnet/minecraft/block/Block;[Lnet/minecraft/block/properties/IProperty;Lcom/google/common/collect/ImmutableMap;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/Cartesian;cartesianProduct(Ljava/lang/Iterable;)Ljava/lang/Iterable;"
            )
    )
    private Iterable<?> onCartesianProduct(Iterable<? extends Iterable<?>> instance) {
        return instance != Collections.EMPTY_LIST ? Cartesian.cartesianProduct(instance) : instance;
    }

    @Redirect(
            method = "<init>(Lnet/minecraft/block/Block;[Lnet/minecraft/block/properties/IProperty;Lcom/google/common/collect/ImmutableMap;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/state/BlockStateContainer;getAllowedValues()Ljava/util/List;"
            )
    )
    private List<?> onGetAllowedValues(BlockStateContainer instance) {
        return !vanillafix$isNumerical() ? getAllowedValues() : Collections.EMPTY_LIST;
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
