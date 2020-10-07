package org.dimdev.vanillafix.crashes.mixins.client;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import org.dimdev.vanillafix.crashes.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
public abstract class MixinTileEntityRenderDispatcher implements StateManager.IResettable {
    @Shadow private boolean drawingBatch;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onInit(CallbackInfo ci) {
        register();
    }

    @Override
    public void resetState() {
        // BufferBuilder is reset from MixinBufferBuilder
        if (drawingBatch) drawingBatch = false;
    }
}
