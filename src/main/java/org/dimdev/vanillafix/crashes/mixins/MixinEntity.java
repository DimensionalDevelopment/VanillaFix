package org.dimdev.vanillafix.crashes.mixins;

import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = 10000) // Always last
public class MixinEntity {
    private boolean noNBT = false;

    @Inject(method = "addEntityCrashInfo", at = @At("TAIL"))
    private void onAddEntityCrashInfo(CrashReportCategory category, CallbackInfo ci) {
        if (!noNBT) {
            noNBT = true;
            category.addDetail("Entity NBT", () -> ((Entity) (Object) this).writeToNBT(new NBTTagCompound()).toString());
            noNBT = false;
        }
    }
}
