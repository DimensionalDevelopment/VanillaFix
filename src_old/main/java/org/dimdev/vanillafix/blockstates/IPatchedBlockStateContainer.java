package org.dimdev.vanillafix.blockstates;

import net.minecraft.block.properties.IProperty;

import java.util.Map;

public interface IPatchedBlockStateContainer {
    Map<IProperty<?>, Integer> getPropertyOffsets();
}
