package org.dimdev.vanillafix.profiler.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.text.TextComponentTranslation;
import org.dimdev.vanillafix.profiler.IPatchedMinecraftServer;
import org.lwjgl.input.Keyboard;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IThreadListener, ISnooperInfo {
    @Shadow @Nullable private IntegratedServer integratedServer;
    @Shadow @Final public Profiler profiler;
    @Shadow public GameSettings gameSettings;
    @Shadow protected abstract void displayDebugInfo(long elapsedTicksTime);

    @Shadow protected abstract void debugFeedbackTranslated(String untranslatedTemplate, Object... objs);
    @Shadow private String debugProfilerName;
    private boolean useIntegratedServerProfiler = false;

    /** @reason Implement using Ctrl + 0-9 to select profiler sections 10-19. */
    @ModifyVariable(method = "updateDebugProfilerName", at = @At("HEAD"), ordinal = 0)
    private int getKeyCountForProfilerNameUpdate(int keyCount) {
        return GuiScreen.isCtrlKeyDown() ? keyCount + 10 : keyCount;
    }

    /** @reason Implement F3 + S to toggle between client and integrated server profilers. */
    @Inject(method = "processKeyF3", at = @At("HEAD"), cancellable = true)
    private void checkF3S(int auxKey, CallbackInfoReturnable<Boolean> cir) {
        if (auxKey == Keyboard.KEY_S) {
            if (integratedServer != null) {
                useIntegratedServerProfiler = !useIntegratedServerProfiler;
                if (useIntegratedServerProfiler) {
                    debugFeedbackTranslated("vanillafix.debug.switch_profiler.server");
                } else {
                    debugFeedbackTranslated("vanillafix.debug.switch_profiler.client");
                }
            }
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    /** @reason Add the F3 + S help message to the F3 + Q debug help menu. */
    @Inject(method = "processKeyF3", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;printChatMessage(Lnet/minecraft/util/text/ITextComponent;)V", ordinal = 9), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addF3SHelpMessage(int auxKey, CallbackInfoReturnable<Boolean> cir, GuiNewChat chatGui) {
        chatGui.printChatMessage(new TextComponentTranslation("vanillafix.debug.switch_profiler.help"));
    }

    /** @reason Use the integrated server profiler rather than client profiler after F3 + S was pressed. */
    @SuppressWarnings("InvalidMemberReference") // https://github.com/minecraft-dev/MinecraftDev/issues/387
    @Redirect(method = {"displayDebugInfo", "updateDebugProfilerName"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;profiler:Lnet/minecraft/profiler/Profiler;"))
    private Profiler getCurrentProfiler(Minecraft minecraft) {
        return useIntegratedServerProfiler && integratedServer != null ? integratedServer.profiler : profiler;
    }

    /**
     * @reason Profiler isn't safe to use async, so get the results from server's last tick if server
     * profilder is being displayed.
     * <p>
     * Note: profilerName is always "root" client-side
     */
    @SuppressWarnings("InvalidMemberReference") // https://github.com/minecraft-dev/MinecraftDev/issues/387
    @Redirect(method = {"displayDebugInfo", "updateDebugProfilerName"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;getProfilingData(Ljava/lang/String;)Ljava/util/List;"))
    private List<Profiler.Result> getProfilerData(Profiler profiler, String profilerName) {
        if (useIntegratedServerProfiler && integratedServer != null) {
            return new ArrayList<>(((IPatchedMinecraftServer) integratedServer).getLastProfilerData());
        } else {
            return profiler.getProfilingData(profilerName);
        }
    }

    /** @reason Get the correct debug profiler name */
    @Redirect(method = "updateDebugProfilerName", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;debugProfilerName:Ljava/lang/String;", opcode = Opcodes.GETFIELD))
    private String getDebugProfilerName(Minecraft mc) {
        if (useIntegratedServerProfiler && integratedServer != null) {
            return ((IPatchedMinecraftServer) integratedServer).getProfilerName();
        } else {
            return debugProfilerName;
        }
    }

    /** @reason Set the correct debug profiler name */
    @Redirect(method = "updateDebugProfilerName", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;debugProfilerName:Ljava/lang/String;", opcode = Opcodes.PUTFIELD))
    private void setDebugProfilerName(Minecraft mc, String debugProfilerName) {
        if (useIntegratedServerProfiler && integratedServer != null) {
            ((IPatchedMinecraftServer) integratedServer).setProfilerName(debugProfilerName);
        } else {
            this.debugProfilerName = debugProfilerName;
        }
    }

    /**
     * @reason Enable profiling for the integrated server too. Reset useIntegratedServerProfiler to
     * false if the integrated server becomes null.
     */
    @Inject(method = "runGameLoop", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;showDebugInfo:Z"))
    private void enableIntegratedServerProfiling(CallbackInfo ci) {
        if (useIntegratedServerProfiler && integratedServer == null) {
            useIntegratedServerProfiler = false;
        }

        if (gameSettings.showDebugInfo && gameSettings.showDebugProfilerChart && useIntegratedServerProfiler && !gameSettings.hideGUI) {
            if (!integratedServer.profiler.profilingEnabled) {
                integratedServer.enableProfiling();
            } else if (((IPatchedMinecraftServer) integratedServer).getLastProfilerData() != null) {
                displayDebugInfo(0);
            }
        } else if (integratedServer != null) {
            integratedServer.profiler.profilingEnabled = false;
        }
    }

    /** @reason Disable client profiling when profiling the integrated server. */
    @Redirect(method = "runGameLoop", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;showDebugProfilerChart:Z"))
    private boolean disableClientProfilingIfUnnecessary(GameSettings gameSettings) {
        return gameSettings.showDebugProfilerChart && !useIntegratedServerProfiler;
    }
}
