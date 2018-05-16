package org.dimdev.vanillafix;

import net.minecraft.client.renderer.chunk.CompiledChunk;

import java.util.HashMap;
import java.util.Map;

// Temporarily stores objects to pass between methods, to avoid having
// to change method parameters using ASM.
public final class TemporaryStorage {
    public static Map<Long, CompiledChunk> currentCompiledChunk = new HashMap<>(); // Thread ID -> Compiled Chunk being rebuilt
}
