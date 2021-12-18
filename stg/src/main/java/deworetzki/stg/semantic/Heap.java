package deworetzki.stg.semantic;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * The {@link Heap} is a mapping from addresses to {@link Closure closures}.
 */
public final class Heap {
    private final AtomicInteger highestUsed = new AtomicInteger();
    private final Queue<Integer> freedAddresses = new ArrayDeque<>();

    private final Map<Integer, Closure> contents = new HashMap<>();

    private synchronized Integer pollFreeAddress() {
        Integer address = freedAddresses.poll();
        if (address != null) return address;
        return highestUsed.getAndIncrement();
    }

    public int allocate(Closure closure) {
        final int address = pollFreeAddress();
        contents.put(address, closure);
        return address;
    }

    public Iterable<Integer> reserveMany(int n) {
        Integer[] reservedAddresses = new Integer[n];
        for (int i = 0; i < n; i++) {
            reservedAddresses[i] = pollFreeAddress();
        }
        return Arrays.asList(reservedAddresses);
    }

    public void update(int address, Closure closure) {
        contents.put(address, closure);
    }

    public Closure get(int address) {
        return contents.get(address);
    }


    // TODO: Define interface
}
