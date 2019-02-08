package com.mike_caron.equivalentintegrations.util;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class Collect
{
    private Collect() {}

    @Nonnull
    private static <T> Set<T> asSet(T item)
    {
        HashSet<T> ret = new HashSet<>();
        ret.add(item);
        return ret;
    }

    @Nonnull
    public static <T> Set<T> asSet(Optional<T> item)
    {
        HashSet<T> ret = new HashSet<>();
        item.ifPresent(ret::add);
        return ret;
    }

    @SafeVarargs
    @Nonnull
    private static <T> Set<T> asSet(T... item)
    {
        HashSet<T> ret = new HashSet<>();
        Collections.addAll(ret, item);
        return ret;
    }
}
