package org.dimdev.vanillafix.blockstates.mixins;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.dimdev.vanillafix.blockstates.NumericalExtendedBlockState;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

@Mixin(ExtendedBlockState.class)
public abstract class MixinExtendedBlockState extends MixinBlockStateContainer {
    public MixinExtendedBlockState(Block block, IProperty<?>[] properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
        super(block, properties, unlistedProperties);
    }

    @Override
    protected IBlockState createState(Map<IProperty<?>, Comparable<?>> properties, @Nullable ImmutableMap<IUnlistedProperty<?>, java.util.Optional<?>> unlistedProperties) {
        IBlockState normalState = super.createState(properties, unlistedProperties);
        if (unlistedProperties == null || unlistedProperties.isEmpty()) {
            return normalState;
        }

        return NumericalExtendedBlockState.getClean(normalState, unlistedProperties);
    }
}
