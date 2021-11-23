package deworetzki.stg.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class FunctionUtils {
    private FunctionUtils() throws IllegalAccessException {
        throw new IllegalAccessException("No instances of this class are allowed!");
    }

    public static <K, V, R> Map<K, R> mapValues(final Map<K, V> map,
                                                final Function<V, R> function,
                                                final IntFunction<Map<K, R>> factory) {
        Map<K, R> result = factory.apply(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            result.put(entry.getKey(), function.apply(entry.getValue()));
        }
        return result;
    }

    public static <A, B, R> Iterable<R> zipWith(final Iterable<A> as, final Iterable<B> bs,
                                                final BiFunction<A, B, R> combinator) {
        return () -> new Zipper<>(as.iterator(), bs.iterator(), combinator);
    }

    public static <A, B, R> Iterator<R> zipWith(final Iterator<A> as, final Iterator<B> bs,
                                                final BiFunction<A, B, R> combinator) {
        return new Zipper<>(as, bs, combinator);
    }

    private record Zipper<A, B, R>(Iterator<A> as, Iterator<B> bs, BiFunction<A, B, R> combinator)
            implements Iterator<R> {

        @Override
        public boolean hasNext() {
            return as.hasNext() && bs.hasNext();
        }

        @Override
        public R next() {
            return combinator.apply(as.next(), bs.next());
        }
    }
}
