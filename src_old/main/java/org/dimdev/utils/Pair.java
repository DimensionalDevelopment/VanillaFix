package org.dimdev.utils;

import java.util.Objects;

public class Pair<L, R> {
    public final L left;
    public final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Pair &&
               Objects.equals(((Pair<?, ?>) obj).left, left) &&
               Objects.equals(((Pair<?, ?>) obj).right, right);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(left) + Objects.hashCode(right) * 31;
    }
}
