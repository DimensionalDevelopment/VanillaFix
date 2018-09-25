package org.dimdev.vanillafix.textures.modsupport.mixins.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.dimdev.vanillafix.textures.IPatchedTextureAtlasSprite;
import org.dimdev.vanillafix.textures.mixins.client.MixinTextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import team.chisel.client.TextureStitcher;

@Pseudo
@SuppressWarnings("deprecation")
@Mixin(TextureStitcher.MagicStitchingSprite.class)
public abstract class MixinMagicStitchingSprite extends MixinTextureAtlasSprite {
    @Shadow private TextureAtlasSprite parent;

    @Override
    public void markNeedsAnimationUpdate() {
        super.markNeedsAnimationUpdate();
        if (parent != null) {
            ((IPatchedTextureAtlasSprite) parent).markNeedsAnimationUpdate();
        }
    }
}
