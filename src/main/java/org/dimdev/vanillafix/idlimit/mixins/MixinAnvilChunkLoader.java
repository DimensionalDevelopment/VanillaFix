package org.dimdev.vanillafix.idlimit.mixins;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.dimdev.vanillafix.idlimit.IPatchedBlockStateContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AnvilChunkLoader.class)
public class MixinAnvilChunkLoader {
    @Inject(method = "readChunkFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;<init>(IZ)V", shift = At.Shift.BY, by = 2), locals = LocalCapture.CAPTURE_FAILHARD)
    private void readPaletteNBT(World world, NBTTagCompound nbt, CallbackInfoReturnable<Chunk> cir,
            int ignored0, int ignored1, Chunk ignored2, NBTTagList ignored3, int ingnored4, ExtendedBlockStorage[] ignored5, boolean ignored6, int ignored7,
            NBTTagCompound storageNBT, int y, ExtendedBlockStorage extendedBlockStorage) {
        int[] palette = storageNBT.hasKey("Palette", 11) ? storageNBT.getIntArray("Palette") : null;
        ((IPatchedBlockStateContainer) extendedBlockStorage.getData()).setTemporaryPalette(palette);
    }

    @Inject(method = "writeChunkToNBT", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/chunk/BlockStateContainer;getDataForNBT([BLnet/minecraft/world/chunk/NibbleArray;)Lnet/minecraft/world/chunk/NibbleArray;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void writePaletteNBT(Chunk chunk, World worldIn, NBTTagCompound nbt, CallbackInfo ci,
            ExtendedBlockStorage[] ignored0, NBTTagList ignored1, boolean ignored2, ExtendedBlockStorage[] ignored3, int ignored4, int ignored5,
            ExtendedBlockStorage extendedBlockStorage, NBTTagCompound storageNBT, byte[] blocks, NibbleArray data, NibbleArray add) {
        int[] palette = ((IPatchedBlockStateContainer) extendedBlockStorage.getData()).getTemporaryPalette();
        if (palette != null) storageNBT.setIntArray("Palette", palette);
    }
}
