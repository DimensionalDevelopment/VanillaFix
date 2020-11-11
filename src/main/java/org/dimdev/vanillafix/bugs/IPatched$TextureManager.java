package org.dimdev.vanillafix.bugs;

import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public interface IPatched$TextureManager {
    Map<ResourceLocation, ITextureObject> getTextures();
}
