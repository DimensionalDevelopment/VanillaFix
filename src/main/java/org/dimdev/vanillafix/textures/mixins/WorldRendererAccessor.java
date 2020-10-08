package org.dimdev.vanillafix.textures.mixins;

import it.unimi.dsi.fastutil.objects.ObjectList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.WorldRenderer;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
    @Accessor("visibleChunks")
    ObjectList<?> getVisibleChunks();
}
