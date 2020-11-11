package org.dimdev.vanillafix.bugs.mixins.client;

import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.dimdev.vanillafix.bugs.IPatched$TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(TextureManager.class)
public class MixinTextureManager implements IPatched$TextureManager {
    @Shadow @Final private Map<ResourceLocation, ITextureObject> mapTextureObjects;

    @Override
    public Map<ResourceLocation, ITextureObject> getTextures() {
        return mapTextureObjects;
    }
}
