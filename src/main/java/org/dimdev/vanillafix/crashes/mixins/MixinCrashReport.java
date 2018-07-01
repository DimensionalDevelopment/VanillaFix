package org.dimdev.vanillafix.crashes.mixins;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dimdev.utils.ModIdentifier;
import org.dimdev.vanillafix.crashes.IPatchedCrashReport;
import org.dimdev.vanillafix.crashes.StacktraceDeobfuscator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Mixin(value = CrashReport.class, priority = 500)
public abstract class MixinCrashReport implements IPatchedCrashReport {
    @Shadow @Final private CrashReportCategory systemDetailsCategory;
    @Shadow @Final private Throwable cause;
    @Shadow @Final private List<CrashReportCategory> crashReportSections;
    @Shadow @Final private String description;

    @Shadow private static String getWittyComment() { return null; }

    private Set<ModContainer> suspectedMods;

    @Override public Set<ModContainer> getSuspectedMods() {
        return suspectedMods;
    }

    /** @reason Adds a list of mods which may have caused the crash to the report. */
    @Inject(method = "populateEnvironment", at = @At("TAIL"))
    private void afterPopulateEnvironment(CallbackInfo ci) {
        systemDetailsCategory.addDetail("Suspected Mods", () -> {
            try {
                suspectedMods = ModIdentifier.identifyFromStacktrace(cause);

                String modListString = "Unknown";
                List<String> modNames = new ArrayList<>();
                for (ModContainer mod : suspectedMods) {
                    modNames.add(mod.getName() + " (" + mod.getModId() + ")");
                }

                if (!modNames.isEmpty()) {
                    modListString = StringUtils.join(modNames, ", ");
                }
                return modListString;
            } catch (Throwable e) {
                return ExceptionUtils.getStackTrace(e).replace("\t", "    ");
            }
        });
    }

    /** @reason Deobfuscates the stacktrace using MCP mappings */
    @Inject(method = "populateEnvironment", at = @At("HEAD"))
    private void beforePopulateEnvironment(CallbackInfo ci) {
        StacktraceDeobfuscator.deobfuscateThrowable(cause);
    }

    /** @reason Improve report formatting */
    @Overwrite
    public String getCompleteReport() {
        StringBuilder builder = new StringBuilder();

        builder.append("---- Minecraft Crash Report ----\n")
               .append("// ").append(getVanillaFixComment())
               .append("\n\n")
               .append("Time: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date())).append("\n")
               .append("Description: ").append(description)
               .append("\n\n")
               .append(stacktraceToString(cause).replace("\t", "    ")) // Vanilla's getCauseStackTraceOrString doesn't print causes and suppressed exceptions
               .append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");

        for (int i = 0; i < 87; i++) {
            builder.append("-");
        }

        builder.append("\n\n");
        getSectionsInStringBuilder(builder);
        return builder.toString().replace("\t", "    ");
    }

    private static String stacktraceToString(Throwable cause) {
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    /** @reason Improve report formatting, add VanillaFix comment */
    @Overwrite
    public void getSectionsInStringBuilder(StringBuilder builder) {
        for (CrashReportCategory crashreportcategory : crashReportSections) {
            crashreportcategory.appendToStringBuilder(builder);
            builder.append("\n");
        }

        systemDetailsCategory.appendToStringBuilder(builder);
    }

    private String getVanillaFixComment() {
        try {
            if (Math.random() < 0.01 && !suspectedMods.isEmpty()) {
                ModContainer mod = suspectedMods.iterator().next();
                String author = mod.getMetadata().authorList.get(0);
                return "I blame " + author + ".";
            }
        } catch (Throwable ignored) {}

        return getWittyComment();
    }
}
