package org.dimdev.vanillafix.modsupport.mixins;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModClassLoader;
import net.minecraftforge.fml.common.ModContainer;
import org.dimdev.vanillafix.VanillaFixLoadingPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.MixinProcessor;
import org.spongepowered.asm.mixin.transformer.Proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.List;

@Mixin(Loader.class)
public class MixinLoader {
    @Shadow(remap = false) private List<ModContainer> mods;
    @Shadow(remap = false) private ModClassLoader modClassLoader;

    /**
     * @reason Load all mods now and load mod support mixin configs. This can't be done later
     * since constructing mods loads classes from them.
     */
    @Inject(method = "loadMods", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/LoadController;transition(Lnet/minecraftforge/fml/common/LoaderState;Z)V", ordinal = 1), remap = false)
    private void beforeConstructingMods(List<String> injectedModContainers, CallbackInfo ci) {
        // Add all mods to class loader
        for (ModContainer mod : mods) {
            try {
                modClassLoader.addFile(mod.getSource());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        // Add and reload mixin configs
        if (VanillaFixLoadingPlugin.config.textureFixes) Mixins.addConfiguration("mixins.vanillafix.textures.modsupport.json");
        Proxy mixinProxy = (Proxy) Launch.classLoader.getTransformers().stream().filter(transformer -> transformer instanceof Proxy).findFirst().get();
        try {
            //This will very likely break on the next major mixin release.
            Class mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");

            Field transformerField = Proxy.class.getDeclaredField("transformer");
            transformerField.setAccessible(true);
            Object transformer = transformerField.get(mixinProxy);

            //Get MixinProcessor from MixinTransformer
            Field processorField = mixinTransformerClass.getDeclaredField("processor");
            processorField.setAccessible(true);
            Object processor = processorField.get(transformer);

            Method selectConfigsMethod = MixinProcessor.class.getDeclaredMethod("selectConfigs", MixinEnvironment.class);
            selectConfigsMethod.setAccessible(true);
            selectConfigsMethod.invoke(processor, MixinEnvironment.getCurrentEnvironment());

            Method prepareConfigsMethod = MixinProcessor.class.getDeclaredMethod("prepareConfigs", MixinEnvironment.class);
            prepareConfigsMethod.setAccessible(true);
            prepareConfigsMethod.invoke(processor, MixinEnvironment.getCurrentEnvironment());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
