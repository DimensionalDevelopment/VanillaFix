package org.dimdev.utils;

import java.util.Objects;

public class LeftIdentityPair<L, R> {
    public final L left;
    public final R right;

    public LeftIdentityPair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LeftIdentityPair &&
               ((LeftIdentityPair) other).left == left &&
               Objects.equals(((LeftIdentityPair) other).right, right);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(left) + Objects.hashCode(right) * 31;
    }
}
