package deworetzki.utils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class CollectionUtils {
    private CollectionUtils() throws IllegalAccessException {
        throw new IllegalAccessException("No instances of this class are allowed!");
    }

    public static <E> List<E> take(int amount, final Deque<E> deque) {
        List<E> result = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            result.add(deque.pop());
        }
        return result;
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

    public static <A, B> void combineWith(final Iterable<A> as, final Iterable<B> bs,
                                          final BiConsumer<A, B> combinator) {
        combineWith(as.iterator(), bs.iterator(), combinator);
    }

    public static <A, B> void combineWith(final Iterator<A> as, final Iterator<B> bs,
                                          final BiConsumer<A, B> combinator) {
        while (as.hasNext() && bs.hasNext())
            combinator.accept(as.next(), bs.next());
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


    @SafeVarargs
    public static <E> Set<E> without(Collection<? extends E> original, Collection<? extends E>... removed) {
        final Set<E> result = new HashSet<>(original);
        for (Collection<? extends E> elementsToRemove : removed) {
            result.removeAll(elementsToRemove);
        }
        return result;
    }

    @SafeVarargs
    public static <E> Set<E> union(Collection<? extends E>... collections) {
        final Set<E> result = new HashSet<>();
        for (Collection<? extends E> elements : collections) {
            result.addAll(elements);
        }
        return result;
    }
}
