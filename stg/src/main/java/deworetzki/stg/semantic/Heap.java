package deworetzki.stg.semantic;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

/**
 * The {@link Heap} is a mapping from addresses to {@link Closure closures}.
 */
public final class Heap {
    private final SortedMap<Integer, Closure> contents = new TreeMap<>();

    private int nextFreeAddress() {
        return contents.lastKey() + 1;
    }

    public Iterable<Integer> reserveMany(int n) {
        final int nextFreeAddress = nextFreeAddress();
        return () -> IntStream.range(nextFreeAddress, nextFreeAddress + n).iterator();
    }

    public void update(int address, Closure closure) {
        contents.put(address, closure);
    }

    public Closure get(int address) {
        return contents.get(address);
    }


    // TODO: Define interface
}
