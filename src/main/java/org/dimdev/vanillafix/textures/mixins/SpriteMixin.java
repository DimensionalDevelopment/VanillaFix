package org.dimdev.vanillafix.textures.mixins;

import org.dimdev.vanillafix.textures.SpriteExtensions;
import org.dimdev.vanillafix.util.config.MixinConfigValue;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.texture.Sprite;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@MixinConfigValue(category = "clientOnly", key = "optimizedAnimatedTextures")
@Mixin(Sprite.class)
public class SpriteMixin implements SpriteExtensions {
    private boolean animationUpdateNeeded;

    @Override
    public void setAnimationUpdateNeeded(boolean animationUpdateNeeded) {
        this.animationUpdateNeeded = animationUpdateNeeded;
    }

    @Override
    public boolean isAnimationUpdateNeeded() {
        return this.animationUpdateNeeded;
    }
}
