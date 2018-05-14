package org.dimdev.vanillafix;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.Set;

public interface IPatchedCompiledChunk {
    public Set<TextureAtlasSprite> getVisibleTextures();
}
