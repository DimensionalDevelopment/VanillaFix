package org.dimdev.vanillafix.bugs.mixins;

import net.minecraft.util.BitArray;
import net.minecraft.world.chunk.BlockStateContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Provided by Aaron from Sponge. All code on this page is used in rights
 * with the Sponge license and was taken from SpongeCommon
 */
@Mixin(BlockStateContainer.class)
public class MixinBlockStateContainer {

    /**
     * Serializing a BlockStateContainer to a PacketBuffer is done in two parts:
     * calculating the size of the allocation needed in the PacketBuffer, and actually
     * writing it.
     *
     * When the BlockStateContainer is actually written to the PacketBuffer,
     * its 'storage.BitArray.getBackingLongArray' is written as a VarInt-length-prefixed
     * array. However, when calculating the size of the allocation needed, the size of
     * 'storage.size()' encoded as a VarInt is used, not the size of 'getBackingLongArray'
     * encoded as a VarInt. If the size of getBackingLongArray is ever large enough to require
     * an extra byte in its VarInt encoding, the allocated buffer will be too small, resuling in a crash.
     *
     * To fix this issue, we calculate the length of getBackingLongArray encoded as a VarInt,
     * when we're calculating the necessary allocation size.
     * @param bits
     * @return
     */
    @Redirect(method = "getSerializedSize", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/BitArray;size()I"))
    private int onGetStorageSize$FixVanillaBug(BitArray bits) {
        return bits.getBackingLongArray().length;
    }

}
