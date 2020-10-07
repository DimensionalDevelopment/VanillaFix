package org.dimdev.vanillafix.crashes.mixins;

import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntity.class, priority = 10000) // Always last
public class MixinTileEntity {
    private boolean noNBT = false;
    @Inject(method = "addInfoToCrashReport", at = @At("TAIL"))
    private void onAddEntityCrashInfo(CrashReportCategory category, CallbackInfo ci) {
        if (!noNBT) {
            noNBT = true;
            // "Block Entity" to stay consistent with vanilla strings
            category.addDetail("Block Entity NBT", () -> ((TileEntity) (Object) this).writeToNBT(new NBTTagCompound()).toString());
            noNBT = false;
        }
    }
}
