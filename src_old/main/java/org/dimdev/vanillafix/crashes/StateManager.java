package org.dimdev.vanillafix.crashes;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Allows registering objects to be reset after a crash. Objects registered
 * use WeakReferences, so they will be garbage-collected despite still being
 * registered here.
 */
public class StateManager {
    public interface IResettable {
        default void register() {
            resettableRefs.add(new WeakReference<>(this));
        }

        void resetState();
    }

    // Use WeakReference to allow garbage collection, preventing memory leaks
    private static Set<WeakReference<IResettable>> resettableRefs = new HashSet<>();

    public static void resetStates() {
        Iterator<WeakReference<IResettable>> iterator = resettableRefs.iterator();
        while (iterator.hasNext()) {
            WeakReference<IResettable> ref = iterator.next();
            if (ref.get() != null) {
                ref.get().resetState();
            } else {
                iterator.remove();
            }
        }
    }
}
