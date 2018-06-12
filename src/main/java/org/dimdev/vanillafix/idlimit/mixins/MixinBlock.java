package org.dimdev.vanillafix.idlimit.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Block.class)
public abstract class MixinBlock {
    @Shadow public static int getIdFromBlock(Block blockIn) { return 0; }
    @Shadow public static Block getBlockById(int id) { return null; }

    /**
     * @reason Use VanillaFix ID format (id, meta rather than meta, id) for blocks with
     * an ID larger than 4096.
     **/
    @Overwrite
    public static int getStateId(IBlockState state) {
        Block block = state.getBlock();
        int id = getIdFromBlock(block);
        int meta = block.getMetaFromState(state);
        if ((id & 0xfffff000) == 0) {
            // Use vanilla 4 bit meta + 12 bit ID
            return id + (meta << 12);
        } else {
            // Use VF 28 bit ID + 4 bit meta
            return (id << 4) + meta;
        }
    }

    /**
     * @reason Use VanillaFix ID format (id, meta rather than meta, id) for blocks with
     * an ID larger than 4096 stored in VF format (state ID is larger than 65536)
     **/
    @Overwrite
    @SuppressWarnings("deprecation")
    public static IBlockState getStateById(int stateID) {
        if ((stateID & 0xffff0000) == 0) {
            // Use vanilla 4 bit meta + 12 bit ID
            int id = stateID & 4095;
            int meta = stateID >> 12 & 15;
            return getBlockById(id).getStateFromMeta(meta);
        } else {
            // Use VF 28 bit ID + 4 bit meta
            int meta = stateID & 0xF;
            int blockID = stateID >> 4;
            return getBlockById(blockID).getStateFromMeta(meta);
        }
    }
}
