package org.dimdev.vanillafix.idlimit.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BitArray;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.IBlockStatePalette;
import net.minecraft.world.chunk.NibbleArray;
import org.dimdev.vanillafix.idlimit.IPatchedBlockStateContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(BlockStateContainer.class)
public abstract class MixinBlockStateContainer implements IPatchedBlockStateContainer {
    @Shadow protected abstract IBlockState get(int index);
    @Shadow protected BitArray storage;
    @Shadow protected IBlockStatePalette palette;
    @Shadow protected abstract void set(int index, IBlockState state);
    @Shadow protected abstract void setBits(int bitsIn);

    private static boolean shouldUseVFFormat;
    private static boolean shouldUseVFFormatDetermined;
    private int[] temporaryPalette; // index -> state id

    private boolean shouldUseVFFormat() {
        if (!shouldUseVFFormatDetermined) {
            for (Block block : Block.REGISTRY) {
                int id = Block.REGISTRY.getIDForObject(block);
                if (id > 4096) {
                    shouldUseVFFormat = true;
                    break;
                }
            }
            shouldUseVFFormatDetermined = true;
        }
        return shouldUseVFFormat;
    }

    @Override
    public int[] getTemporaryPalette() {
        return temporaryPalette;
    }

    @Override
    public void setTemporaryPalette(int[] temporaryPalette) {
        this.temporaryPalette = temporaryPalette;
    }

    /**
     * @reason If this BlockStateContainer should be saved in VanillaFix format,
     * store palette IDs rather than block IDs in the container's "Blocks" and
     * "Data" arrays.
     */
    @SuppressWarnings("deprecation")
    @Inject(method = "getDataForNBT", at = @At("HEAD"), cancellable = true)
    private void getDataForNBTVF(byte[] blockIds, NibbleArray data, CallbackInfoReturnable<NibbleArray> cir) {
        if (!shouldUseVFFormat()) return; // Save containters in VF format only if 4096 IDs are being used TODO: only if this containter contains IDs >4096?

        HashMap<IBlockState, Integer> stateIDMap = new HashMap<>();
        int nextID = 0;
        for (int index = 0; index < 4096; ++index) {
            IBlockState state = get(index);
            Integer paletteID = stateIDMap.get(state);
            if (paletteID == null) {
                paletteID = nextID++;
                stateIDMap.put(state, paletteID);
            }

            int x = index & 15;
            int y = index >> 8 & 15;
            int z = index >> 4 & 15;

            blockIds[index] = (byte) (paletteID >> 4 & 255);
            data.set(x, y, z, paletteID & 15);
        }

        temporaryPalette = new int[nextID];
        for (Map.Entry<IBlockState, Integer> entry : stateIDMap.entrySet()) {
            temporaryPalette[entry.getValue()] = Block.BLOCK_STATE_IDS.get(entry.getKey());
        }

        cir.setReturnValue(null);
        cir.cancel();
    }

    /**
     * @reason If this BlockStateContainer is saved in VanillaFix format, treat
     * the "Blocks" and "Data" arrays as palette IDs.
     */
    @SuppressWarnings("deprecation")
    @Inject(method = "setDataFromNBT", at = @At("HEAD"), cancellable = true)
    private void setDataFromNBTVF(byte[] blockIds, NibbleArray data, NibbleArray blockIdExtension, CallbackInfo ci) {
        if (temporaryPalette == null) return; // Read containers in VF format only if container was saved in VF format (has a palette)

        for (int index = 0; index < 4096; ++index) {
            int x = index & 15;
            int y = index >> 8 & 15;
            int z = index >> 4 & 15;
            int paletteID = (blockIds[index] & 255) << 4 | data.get(x, y, z);

            set(index, Block.BLOCK_STATE_IDS.getByValue(temporaryPalette[paletteID]));
        }

        temporaryPalette = null;
        ci.cancel();
    }
}
