package org.dimdev.utils;

public class IdentityPair<L, R> {
    public final L left;
    public final R right;

    public IdentityPair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof IdentityPair &&
               ((IdentityPair) other).left == left &&
               ((IdentityPair) other).right == right;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(left) + System.identityHashCode(right) * 31;
    }
}
