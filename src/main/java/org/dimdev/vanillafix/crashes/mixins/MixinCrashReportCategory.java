package org.dimdev.vanillafix.crashes.mixins;

import net.minecraft.crash.CrashReportCategory;
import org.dimdev.vanillafix.crashes.StacktraceDeobfuscator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CrashReportCategory.class)
public class MixinCrashReportCategory {
    /**
     * @reason Deobfuscate stacktrace for crash report categories.
     */
    @Inject(method = "getPrunedStackTrace", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;getStackTrace()[Ljava/lang/StackTraceElement;", shift = At.Shift.BY, by = 2, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterGetStacktrace(int size, CallbackInfoReturnable<Integer> cir, StackTraceElement[] stackTrace) {
        int index = 0;
        for (StackTraceElement el : stackTrace) {
            stackTrace[index++] = new StackTraceElement(el.getClassName(), StacktraceDeobfuscator.deobfuscateMethodName(el.getMethodName()), el.getFileName(), el.getLineNumber());
        }
    }
}
