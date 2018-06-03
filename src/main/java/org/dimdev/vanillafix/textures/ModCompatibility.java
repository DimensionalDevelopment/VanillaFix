package org.dimdev.vanillafix.textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import team.chisel.client.TextureStitcher;

import java.lang.reflect.Field;

@SuppressWarnings("deprecation")
public final class ModCompatibility { // Needs to be in a separate class to avoid classloading problems with Mixin

    private static Field magicStitchingSpriteParent;
    private static boolean chiselLoaded;

    static {
        if (Loader.isModLoaded("chisel") && Loader.instance().getModState(Loader.instance().getIndexedModList().get("chisel")) != LoaderState.ModState.UNLOADED) {
            chiselLoaded = true;
            try {
                magicStitchingSpriteParent = TextureStitcher.MagicStitchingSprite.class.getDeclaredField("parent");
                magicStitchingSpriteParent.setAccessible(true);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void markDependentTextures(TextureAtlasSprite texture) {
        if (chiselLoaded && texture instanceof TextureStitcher.MagicStitchingSprite) {
            try {
                TextureAtlasSprite newTexture = (TextureAtlasSprite) magicStitchingSpriteParent.get(texture);
                ((IPatchedTextureAtlasSprite) newTexture).markNeedsAnimationUpdate();
                markDependentTextures(texture);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
