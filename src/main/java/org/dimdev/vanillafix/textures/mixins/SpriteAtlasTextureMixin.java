package org.dimdev.vanillafix.textures.mixins;

import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;
import org.dimdev.vanillafix.textures.ChunkDataExtensions;
import org.dimdev.vanillafix.textures.SpriteExtensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.profiler.Profiler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(SpriteAtlasTexture.class)
public abstract class SpriteAtlasTextureMixin extends AbstractTexture {

	@Shadow
	@Final
	private List<Sprite> animatedSprites;

	/**
	 * @reason Replaces the tickAnimatedSprites method to only tick animated textures
	 * that are in one of the loaded BuiltChunks. This can lead to an FPS more than
	 * three times higher on large modpacks with many textures.
	 * <p>
	 * Also breaks down the "root.tick.textures" profiler by texture name.
	 * @author Runemoro
	 */
	@Overwrite
	public void tickAnimatedSprites() {
		Profiler profiler = MinecraftClient.getInstance().getProfiler();
		profiler.push("determineVisibleTextures");
		for (Object e : ((WorldRendererAccessor) Objects.requireNonNull(MinecraftClient.getInstance().worldRenderer)).getVisibleChunks()) {
			ChunkBuilder.BuiltChunk builtChunk = ((ChunkInfoAccessor) e).getChunk();
			for (Sprite sprite : ((ChunkDataExtensions) builtChunk.getData()).getVisibleTextures()) {
				((SpriteExtensions) sprite).setAnimationUpdateRequired(true);
			}
		}
		profiler.pop();

		RenderSystem.bindTexture(this.getGlId());
		for (Sprite animatedSprite : this.animatedSprites) {
			if (((SpriteExtensions) animatedSprite).isAnimationUpdateRequired()) {
				profiler.push(animatedSprite.getId().toString());
				animatedSprite.tickAnimation();
				((SpriteExtensions) animatedSprite).setAnimationUpdateRequired(false);
				profiler.pop();
			}
		}
	}
}
