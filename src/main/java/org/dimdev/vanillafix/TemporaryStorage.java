package org.dimdev.vanillafix;

import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Temporarily stores objects to pass between methods, to avoid having
// to change method parameters using ASM.
public final class TemporaryStorage {
    public static Map<Long, CompiledChunk> currentCompiledChunk = new HashMap<>(); // Thread ID -> Compiled Chunk being rebuilt
}
