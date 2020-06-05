package org.dimdev.vanillafix.bugs.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.dimdev.vanillafix.bugs.IPatched$TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Map;

/**
 * Makes interdimensional teleportation nearly as fast as same-dimension
 * teleportation by removing the "Downloading terrain..." screen. This will cause
 * the player to see partially loaded terrain rather than waiting for the whole
 * render distance to load, but that's also the vanilla behaviour for same-dimension
 * teleportation.
 *
 * Forces {@link TextureManager} to unload all skins it has.
 * See https://bugs.mojang.com/browse/MC-186052
 */
@Mixin(value = NetHandlerPlayClient.class, priority = 500)
public abstract class MixinNetHandlerPlayClient implements INetHandlerPlayClient {
    @Shadow private Minecraft client;

    @Redirect(method = "handleJoinGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    private void onGuiDisplayJoin(Minecraft mc, GuiScreen guiScreenIn) {
        mc.displayGuiScreen(null);
    }

    @Redirect(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    private void onGuiDisplayRespawn(Minecraft mc, GuiScreen guiScreenIn) {
        mc.displayGuiScreen(null);
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void onDisconnect(ITextComponent reason, CallbackInfo ci) {
        TextureManager renderEngine = this.client.getTextureManager();
        Map<ResourceLocation, ITextureObject> textures = ((IPatched$TextureManager)renderEngine).getTextures();
        Iterator<Map.Entry<ResourceLocation, ITextureObject>> iterator = textures.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ResourceLocation, ITextureObject> entry = iterator.next();
            if (entry.getKey().getPath().startsWith("skins/")) {
                iterator.remove();
                ITextureObject texture = entry.getValue();
                if (texture instanceof AbstractTexture) {
                    ((AbstractTexture) texture).deleteGlTexture();
                } else {
                    GlStateManager.deleteTexture(texture.getGlTextureId());
                }
            }
        }
    }
}
