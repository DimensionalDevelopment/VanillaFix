package org.dimdev.vanillafix;

import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ModCompatibilityMixinPlugin implements IMixinConfigPlugin {
    private boolean spongeInstalled;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            spongeInstalled = Launch.classLoader.getClassBytes("org.spongepowered.mod.SpongeCoremod") != null;
        } catch (IOException e) {
            throw new RuntimeException(e); // Should never happen
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    @SuppressWarnings("RedundantIfStatement")
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Sponge
        if (spongeInstalled) {
            if (mixinClassName.equals("org.dimdev.vanillafix.profiler.mixins.MixinWorld")) return false;
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
