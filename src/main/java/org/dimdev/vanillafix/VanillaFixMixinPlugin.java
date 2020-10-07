package org.dimdev.vanillafix;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.dimdev.vanillafix.util.config.MixinConfigValue;
import org.dimdev.vanillafix.util.config.ModConfig;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class VanillaFixMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        try {
            Class<?> clazz = Class.forName(mixinClassName);
            if (clazz.getAnnotation(MixinConfigValue.class) == null) {
                return true;
            }
            MixinConfigValue mixinConfigValue = clazz.getAnnotation(MixinConfigValue.class);
            try {
                Field categoryField = ModConfig.class.getDeclaredField(mixinConfigValue.category());
                Object category = categoryField.get(VanillaFix.modConfig);
                Field keyField = category.getClass().getDeclaredField(mixinConfigValue.key());
                return keyField.getBoolean(category);
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException("Invalid Category or Config Key declared", e);
            } catch (IllegalAccessException e) {
                throw new AssertionError("Can't happen!", e);
            }
        } catch (ClassNotFoundException e) {
            throw (NoClassDefFoundError) new NoClassDefFoundError().initCause(e);
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() { return null; }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}
