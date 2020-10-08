package org.dimdev.vanillafix.textures.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(BakedQuad.class)
public interface BakedQuadAccessor {
    @Accessor
    Sprite getSprite();
}
