package org.dimdev.vanillafix.profiler.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.util.IThreadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IThreadListener, ISnooperInfo {
    /**
     * @reason Implement using Ctrl + 0-9 to select profiler sections 10-19.
     */
    @ModifyVariable(method = "updateDebugProfilerName", at = @At("HEAD"), ordinal = 0)
    private int getKeyCountForProfilerNameUpdate(int keyCount) {
        return GuiScreen.isCtrlKeyDown() ? keyCount + 10 : keyCount;
    }
}
