package org.dimdev.vanillafix.bugs.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ScreenshotEvent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IThreadListener, ISnooperInfo {
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final public Profiler profiler;

    @Shadow public GuiIngame ingameGUI;

    /** @reason Fix GUI logic being included as part of "root.tick.textures" (https://bugs.mojang.com/browse/MC-129556) */
    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", ordinal = 0))
    private void endStartGUISection(Profiler profiler, String name) {
        profiler.endStartSection("gui");
    }

    /** @reason Part 2 of GUI logic fix. */
    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;tick()V", ordinal = 0))
    private void tickTextureManagerWithCorrectProfiler(TextureManager textureManager) {
        profiler.endStartSection("textures");
        textureManager.tick();
        profiler.endStartSection("gui");
    }

    /** @reason Make saving screenshots async (https://bugs.mojang.com/browse/MC-33383) */
    @Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ScreenShotHelper;saveScreenshot(Ljava/io/File;IILnet/minecraft/client/shader/Framebuffer;)Lnet/minecraft/util/text/ITextComponent;", ordinal = 0))
    private ITextComponent saveScreenshotAsync(File gameDirectory, int width, int height, Framebuffer buffer) {
        try {
            final BufferedImage screenshot = ScreenShotHelper.createScreenshot(width, height, buffer);

            new Thread(() -> {
                try {
                    File screenshotDir = new File(gameDirectory, "screenshots");
                    screenshotDir.mkdir();
                    File screenshotFile = ScreenShotHelper.getTimestampedPNGFileForDirectory(screenshotDir).getCanonicalFile();

                    // Forge event
                    ScreenshotEvent event = ForgeHooksClient.onScreenshot(screenshot, screenshotFile);
                    if (event.isCanceled()) {
                        ingameGUI.getChatGUI().printChatMessage(event.getCancelMessage());
                        return;
                    } else {
                        screenshotFile = event.getScreenshotFile();
                    }

                    ImageIO.write(screenshot, "png", screenshotFile);

                    // Forge event
                    if (event.getResultMessage() != null) {
                        ingameGUI.getChatGUI().printChatMessage(event.getResultMessage());
                        return;
                    }

                    ITextComponent screenshotLink = new TextComponentString(screenshotFile.getName());
                    screenshotLink.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, screenshotFile.getAbsolutePath()));
                    screenshotLink.getStyle().setUnderlined(true);
                    ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("screenshot.success", screenshotLink));
                } catch (Exception e) {
                    LOGGER.warn("Couldn't save screenshot", e);
                    ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("screenshot.failure", e.getMessage()));
                }
            }, "Screenshot Saving Thread").start();
        } catch (Exception e) {
            LOGGER.warn("Couldn't save screenshot", e);
            ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("screenshot.failure", e.getMessage()));
        }

        return null;
    }

    /** @reason Message is sent from screenshot method now. */
    @Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;printChatMessage(Lnet/minecraft/util/text/ITextComponent;)V", ordinal = 0))
    private void sendScreenshotMessage(GuiNewChat guiNewChat, ITextComponent chatComponent) {}

    /** @reason Removes a call to {@link System#gc()} to make world loading as fast as possible */
    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Ljava/lang/System;gc()V"), cancellable = true)
    private void onSystemGC(WorldClient worldClient, String reason, CallbackInfo ci) {
        ci.cancel();
    }
}
