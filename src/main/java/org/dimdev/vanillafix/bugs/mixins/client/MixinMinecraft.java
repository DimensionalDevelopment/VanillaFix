package org.dimdev.vanillafix.bugs.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.IThreadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IThreadListener, ISnooperInfo {
    @Shadow @Final public Profiler mcProfiler;

    /**
     * @reason Fix GUI logic being included as part of "root.tick.textures" (https://bugs.mojang.com/browse/MC-129556)
     */
    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", ordinal = 0))
    private void endStartSectionTextures(Profiler profiler, String name) {
        profiler.endStartSection("gui");
    }

    /**
     * @reason Part 2 of GUI logic fix.
     */
    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;tick()V", ordinal = 0))
    private void textureManagerTick(TextureManager textureManager) {
        mcProfiler.endStartSection("textures");
        textureManager.tick();
        mcProfiler.endStartSection("gui");
    }
}
