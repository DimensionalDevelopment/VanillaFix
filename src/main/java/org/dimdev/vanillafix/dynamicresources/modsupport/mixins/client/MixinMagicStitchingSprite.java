package org.dimdev.vanillafix.dynamicresources.modsupport.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.chisel.client.TextureStitcher;

@Pseudo
@Mixin(TextureStitcher.MagicStitchingSprite.class)
@SuppressWarnings("deprecation")
public abstract class MixinMagicStitchingSprite extends TextureAtlasSprite {
    @Shadow abstract void postStitch();
    @Shadow private TextureAtlasSprite parent;

    public MixinMagicStitchingSprite(String spriteName) {
        super(spriteName);
    }

    @Inject(
            method = "<init>(Ljava/lang/String;)V",
            at = @At(
                    value = "RETURN"
            )
    )
    private void onInit(String spriteName, CallbackInfo callbackInfo) {
        this.parent = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(spriteName);
        postStitch();
    }
}
