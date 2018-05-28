package org.dimdev.vanillafix.crashes.mixins;

import net.minecraft.crash.CrashReportCategory;
import org.dimdev.vanillafix.crashes.StacktraceDeobfuscator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(CrashReportCategory.class)
public class MixinCrashReportCategory {
    @Shadow @Final private String name;
    @Shadow @Final private List<CrashReportCategory.Entry> children;

    /** @reason Deobfuscate stacktrace for crash report categories. */
    @Inject(method = "getPrunedStackTrace", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;getStackTrace()[Ljava/lang/StackTraceElement;", shift = At.Shift.BY, by = 2, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterGetStacktrace(int size, CallbackInfoReturnable<Integer> cir, StackTraceElement[] stackTrace) {
        StacktraceDeobfuscator.deobfuscateStacktrace(stackTrace);
    }

    /** @reason Improve crash report formatting **/
    @Overwrite
    public void appendToStringBuilder(StringBuilder builder) {
        builder.append("-- ").append(name).append(" --\n");
        for (CrashReportCategory.Entry entry : children) {
            String sectionIndent = "  ";

            builder.append(sectionIndent)
                   .append(entry.getKey())
                   .append(": ");

            StringBuilder indent = new StringBuilder(sectionIndent + "  ");
            for (char ignored : entry.getKey().toCharArray()) {
                indent.append(" ");
            }

            boolean first = true;
            for (String line : entry.getValue().trim().split("\n")) {
                if (!first) builder.append("\n").append(indent);
                first = false;
                if (line.startsWith("\t")) line = line.substring(1);
                builder.append(line.replace("\t", ""));
            }

            builder.append("\n");
        }
    }
}
