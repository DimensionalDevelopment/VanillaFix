package org.dimdev.vanillafix.textures;

import java.util.Set;

import net.minecraft.client.texture.Sprite;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ChunkDataExtensions {
	Set<Sprite> getVisibleTextures();
}

