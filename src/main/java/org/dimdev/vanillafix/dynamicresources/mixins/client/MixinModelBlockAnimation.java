package org.dimdev.vanillafix.dynamicresources.mixins.client;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.animation.ModelBlockAnimation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModelBlockAnimation.class)
public class MixinModelBlockAnimation {
    @Shadow @Final private static ModelBlockAnimation defaultModelBlockAnimation;

    /**
     * @reason This is missing for almost all models, causing an IOException to be
     * thrown, which makes model loading very slow. Instead, use getAllResources.
     * <p>
     * See <a href="https://github.com/MinecraftForge/MinecraftForge/issues/5048">https://github.com/MinecraftForge/MinecraftForge/issues/5048</a>
     * <p>
     * TODO: Optimize this, or disable this for certain forge versions once Forge fixes it
     */
    @Overwrite
    public static ModelBlockAnimation loadVanillaAnimation(IResourceManager manager, ResourceLocation armatureLocation) {
        return defaultModelBlockAnimation;
    }
}
