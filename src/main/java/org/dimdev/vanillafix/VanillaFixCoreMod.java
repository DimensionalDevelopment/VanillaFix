package org.dimdev.vanillafix;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(5000)
@IFMLLoadingPlugin.TransformerExclusions("org.dimdev.vanillafix.")
public class VanillaFixCoreMod implements IFMLLoadingPlugin {

    public VanillaFixCoreMod() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.vanillafix.json");
    }

    @Override public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override public String getModContainerClass() {
        return "org.dimdev.vanillafix.VanillaFixCoreContainer";
    }

    @Nullable @Override public String getSetupClass() {
        return null;
    }

    @Override public void injectData(Map<String, Object> data) {}

    @Override public String getAccessTransformerClass() {
        return null;
    }
}
