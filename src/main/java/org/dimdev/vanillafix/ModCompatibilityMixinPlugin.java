package org.dimdev.vanillafix;

import com.google.common.collect.ImmutableSet;
import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Determines which mixins should not be loaded because of compatibility problems.
 */
public class ModCompatibilityMixinPlugin implements IMixinConfigPlugin {

    private static final Set<String> NON_SPONGE_MIXINS = ImmutableSet.of(
            "org.dimdev.vanillafix.profiler.mixins.MixinWorld"
    );

    private boolean spongeInstalled;

    @Override
    public void onLoad(String mixinPackage) {
        spongeInstalled = isSpongeInstalled();
    }

    private boolean isSpongeInstalled() {
        try {
            return Launch.classLoader.getClassBytes("org.spongepowered.mod.SpongeCoremod") != null;
        } catch (IOException e) {
            throw new RuntimeException(e); // Should never happen
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (NON_SPONGE_MIXINS.contains(mixinClassName)) {
            return !spongeInstalled;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
