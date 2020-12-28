package org.dimdev.vanillafix.textures;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface SpriteExtensions {
	void setAnimationUpdateRequired(boolean animationUpdateRequired);

	boolean isAnimationUpdateRequired();
}
