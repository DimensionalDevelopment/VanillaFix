package org.dimdev.vanillafix.textures;

import net.minecraft.client.renderer.chunk.CompiledChunk;

/**
 * Temporarily stores objects to pass between methods, to avoid
 * having to change method parameters.
 */
public final class TemporaryStorage {
    public static ThreadLocal<CompiledChunk> currentCompiledChunk = new ThreadLocal<>(); // Thread ID -> Compiled Chunk being rebuilt
}
