package org.dimdev.vanillafix.dynamicresources.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import org.dimdev.vanillafix.dynamicresources.DynamicTextureMap;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow private TextureMap textureMapBlocks;

    /**
     * @reason Replace the default texture map with a dynamic texture map that can stitch
     * and unstitch textures while playing, rather than at init.
     */
    @Redirect(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;textureMapBlocks:Lnet/minecraft/client/renderer/texture/TextureMap;", opcode = Opcodes.PUTFIELD))
    private void setTextureMapBlocks(Minecraft minecraft, TextureMap value) {
        textureMapBlocks = new DynamicTextureMap("textures");
    }

    /**
     * @reason Update the texture map every tick, to upload textures and resize the atlas
     * if necessary.
     */
    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V", ordinal = 0))
    private void runGameLoop(CallbackInfo ci) {
        ((DynamicTextureMap) textureMapBlocks).update();
    }
}
