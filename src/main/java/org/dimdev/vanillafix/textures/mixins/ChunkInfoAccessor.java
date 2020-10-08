package org.dimdev.vanillafix.textures.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.chunk.ChunkBuilder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(targets = "net.minecraft.client.render.WorldRenderer$ChunkInfo")
public interface ChunkInfoAccessor {
    @Accessor
    ChunkBuilder.BuiltChunk getChunk();
}
