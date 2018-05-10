package org.dimdev.vanillafix.mixins;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dimdev.utils.ModIdentifier;
import org.dimdev.vanillafix.IPatchedCrashReport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(CrashReport.class)
public class MixinCrashReport implements IPatchedCrashReport {
    @Shadow @Final private CrashReportCategory systemDetailsCategory;
    @Shadow @Final private Throwable cause;

    private Set<ModContainer> suspectedMods;

    @Override public Set<ModContainer> getSuspectedMods() {
        return suspectedMods;
    }

    @Inject(method = "populateEnvironment", at = @At("TAIL"))
    public void afterPopulateEnvironment(CallbackInfo ci) {
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
                return ExceptionUtils.getStackTrace(e);
            }
        });
    }
}
