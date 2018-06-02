package org.dimdev.vanillafix.textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import team.chisel.client.TextureStitcher;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

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

    public static void addDependentTextures(Set<TextureAtlasSprite> visibleTextures) {
        Set<TextureAtlasSprite> textures = visibleTextures;
        Set<TextureAtlasSprite> newTextures;
        do {
            newTextures = new HashSet<>();
            for (TextureAtlasSprite texture : textures) {
                if (chiselLoaded && texture instanceof TextureStitcher.MagicStitchingSprite) {
                    try {
                        newTextures.add((TextureAtlasSprite) magicStitchingSpriteParent.get(texture));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            visibleTextures.addAll(newTextures);
            textures = newTextures;
        } while (newTextures.size() != 0);
    }
}
