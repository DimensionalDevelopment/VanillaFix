package org.dimdev.vanillafix.textures.modsupport.mixins.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.dimdev.vanillafix.textures.IPatchedTextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.client.GuiUtil;

@Pseudo
@Mixin(GuiUtil.class)
public class MixinGuiUtil {
    @Inject(method = "renderTiledTextureAtlas", at = @At("HEAD"), remap = false)
    private static void onRenderTiledTextureAtlas(int x, int y, int width, int height, float depth, TextureAtlasSprite sprite, boolean upsideDown, CallbackInfo ci) {
        ((IPatchedTextureAtlasSprite) sprite).markNeedsAnimationUpdate();
    }
}
