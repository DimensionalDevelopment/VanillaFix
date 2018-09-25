package org.dimdev.vanillafix.textures.mixins.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.dimdev.vanillafix.textures.IPatchedTextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextureAtlasSprite.class)
public abstract class MixinTextureAtlasSprite implements IPatchedTextureAtlasSprite {
    @Shadow public abstract void copyFrom(TextureAtlasSprite atlasSpirit);

    private boolean needsAnimationUpdate = false;

    @Override
    public void markNeedsAnimationUpdate() {
        needsAnimationUpdate = true;
    }

    @Override
    public boolean needsAnimationUpdate() {
        return needsAnimationUpdate;
    }

    @Override
    public void unmarkNeedsAnimationUpdate() {
        needsAnimationUpdate = false;
    }
}
