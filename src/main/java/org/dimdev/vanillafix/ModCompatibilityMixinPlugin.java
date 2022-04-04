package org.dimdev.vanillafix;

import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ModCompatibilityMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.equals("org.dimdev.vanillafix.profiler.mixins.MixinWorld")) {
            return !classExists("org.spongepowered.mod.SpongeCoremod");
        }

        if (mixinClassName.equals("org.dimdev.vanillafix.textures.mixins.client.MixinBlockModelRenderer")) {
            return !classExists("optifine.OptiFineForgeTweaker");
        }

        if (mixinClassName.equals("org.dimdev.vanillafix.textures.mixins.client.MixinBlockModelRendererOptifine")) {
            return classExists("optifine.OptiFineForgeTweaker");
        }

        return true;
    }

    public boolean classExists(String name) {
        try {
            return Launch.classLoader.getClassBytes(name) != null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }
    
    @Override
    public final void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // no-op
    }
    
    // Mixin 0.7 Compatibility
    public final void preApply(String targetClassName, org.spongepowered.asm.lib.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // no-op
    }
    
    @Override
    public final void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // no-op
    }
    
    // Mixin 0.7 Compatibility
    public final void postApply(String targetClassName, org.spongepowered.asm.lib.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // no-op
    }
}
