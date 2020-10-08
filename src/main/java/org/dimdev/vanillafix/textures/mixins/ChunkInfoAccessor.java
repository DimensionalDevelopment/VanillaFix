package org.dimdev.vanillafix.textures.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.chunk.ChunkBuilder;

@Mixin(targets = "net.minecraft.client.render.WorldRenderer$ChunkInfo")
public interface ChunkInfoAccessor {
    @Accessor
    ChunkBuilder.BuiltChunk getChunk();
}
