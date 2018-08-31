package org.dimdev.vanillafix.dynamicresources.modsupport.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import team.chisel.client.TextureStitcher;

@Pseudo
@Mixin(TextureStitcher.MagicStitchingSprite.class)
@SuppressWarnings("deprecation")
public abstract class MixinMagicStitchingSprite extends TextureAtlasSprite {
    @Shadow abstract void postStitch();
    @Shadow private TextureAtlasSprite parent;

    @Overwrite(remap = false)
    public MixinMagicStitchingSprite(String spriteName) {
        super(spriteName);
        parent = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(spriteName);
        postStitch();
    }
}
