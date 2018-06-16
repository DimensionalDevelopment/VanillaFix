package org.dimdev.vanillafix.crashes.mixins.client;

import net.minecraft.client.renderer.BufferBuilder;
import org.dimdev.vanillafix.crashes.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements StateManager.IResettable {
    @Shadow private boolean isDrawing;
    @Shadow public abstract void finishDrawing();

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onInit(int bufferSizeIn, CallbackInfo ci) {
        register();
    }

    @Override
    public void resetState() {
        if (isDrawing) finishDrawing();
    }
}
