package org.dimdev.vanillafix.textures;

public interface IPatchedTextureAtlasSprite {
    void markNeedsAnimationUpdate();
    boolean needsAnimationUpdate();
    void unmarkNeedsAnimationUpdate();
}
