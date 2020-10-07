package org.dimdev.vanillafix.dynamicresources.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.text.TextComponentTranslation;
import org.dimdev.vanillafix.dynamicresources.DynamicTextureMap;
import org.dimdev.vanillafix.dynamicresources.IPatchedMinecraft;
import org.dimdev.vanillafix.dynamicresources.TextureMapRenderer;
import org.lwjgl.input.Keyboard;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IPatchedMinecraft {
    @Shadow private TextureMap textureMapBlocks;
    @Shadow public TextureManager renderEngine;
    private boolean doneLoading = false;

    @Shadow protected abstract void debugFeedbackTranslated(String untranslatedTemplate, Object... objs);

    private boolean drawTextureMap;

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
    private void beforeRender(CallbackInfo ci) {
        ((DynamicTextureMap) textureMapBlocks).update();
    }

    /** @reason Implement F3 + M to draw texture map. */
    @Inject(method = "processKeyF3", at = @At("HEAD"), cancellable = true)
    private void checkF3S(int auxKey, CallbackInfoReturnable<Boolean> cir) {
        if (auxKey == Keyboard.KEY_M) {
            drawTextureMap = !drawTextureMap;
            if (drawTextureMap) {
                debugFeedbackTranslated("vanillafix.debug.draw_texture_map.enabled");
            } else {
                debugFeedbackTranslated("vanillafix.debug.draw_texture_map.disabled");
            }
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    /** @reason Add the F3 + M help message to the F3 + Q debug help menu. */
    @Inject(method = "processKeyF3", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;printChatMessage(Lnet/minecraft/util/text/ITextComponent;)V", ordinal = 9), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addF3SHelpMessage(int auxKey, CallbackInfoReturnable<Boolean> cir, GuiNewChat chatGui) {
        chatGui.printChatMessage(new TextComponentTranslation("vanillafix.debug.draw_texture_map.help"));
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/shader/Framebuffer;unbindFramebuffer()V", ordinal = 0))
    private void onRenderEnd(CallbackInfo ci) {
        if (drawTextureMap) {
            Minecraft.getMinecraft().profiler.startSection("root");
            Minecraft.getMinecraft().profiler.startSection("drawTextureMap");
            TextureMapRenderer.draw();
            Minecraft.getMinecraft().profiler.endSection();
            Minecraft.getMinecraft().profiler.endSection();
        }
    }

    @Inject(method = "init", at = @At(value = "RETURN"))
    private void onDoneLoading(CallbackInfo ci) {
        doneLoading = true;
    }

    @Override
    public boolean isDoneLoading() {
        return doneLoading;
    }
}
