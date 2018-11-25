package org.dimdev.vanillafix.textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ChunkSpriteStorage {
    private static Logger log = LogManager.getLogger ();
    private static ReadWriteLock lock = new ReentrantReadWriteLock ();
    private static Map<TextureAtlasSprite, Integer> chunkSprites = new TreeMap<> (Comparator.comparing (TextureAtlasSprite::toString));

    public static void addUsage (TextureAtlasSprite sprite) {
        lock.writeLock ().lock ();
        try {
            if (chunkSprites.containsKey (sprite)) {
                chunkSprites.put (sprite, chunkSprites.get (sprite) + 1);
            } else {
                chunkSprites.put (sprite, 1);
            }
        } finally {
            lock.writeLock ().unlock ();
        }
    }

    public static void removeUsage (TextureAtlasSprite sprite) {
        lock.writeLock ().lock ();
        try {
            if (chunkSprites.containsKey (sprite)) {
                int i = chunkSprites.get (sprite);
                if (i <= 1) {
                    chunkSprites.remove (sprite);
                } else {
                    chunkSprites.put (sprite, i - 1);
                }
            } else {
                log.error ("Tried to remove sprite, that has no usage");
            }
        } finally {
            lock.writeLock ().unlock ();
        }
    }

    public static void markAnimationsForUpdate () {
        lock.readLock ().lock ();
        try {
            for (TextureAtlasSprite sprite : chunkSprites.keySet ()) {
                ((IPatchedTextureAtlasSprite) sprite).markNeedsAnimationUpdate ();
            }
        } finally {
            lock.readLock ().unlock ();
        }
    }
}
