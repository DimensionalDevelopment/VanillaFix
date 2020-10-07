package org.dimdev.vanillafix.textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.Set;

public interface IPatchedCompiledChunk {
    Set<TextureAtlasSprite> getVisibleTextures();
}
