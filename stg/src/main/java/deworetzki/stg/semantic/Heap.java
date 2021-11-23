package deworetzki.stg.semantic;

import deworetzki.stg.syntax.Bind;
import deworetzki.stg.syntax.Program;
import deworetzki.stg.syntax.Variable;

import java.util.*;

import static deworetzki.stg.semantic.Value.Address;
import static deworetzki.stg.utils.FunctionUtils.mapValues;

/**
 * The {@link Heap} is a mapping from addresses to {@link Closure closures}.
 */
public final class Heap {
    private final SortedMap<Integer, Closure> contents = new TreeMap<>();

    public int allocate(final Closure closure) {
        int nextFreeAddress = contents.lastKey() + 1;
        contents.put(nextFreeAddress, closure);
        return nextFreeAddress;
    }

    // TODO: Define interface

    public Map<Variable, Value> initialize(final Program program) {
        Map<Variable, Integer> globalEnvironment = new HashMap<>();

        // Collect Heap addresses for every Bind.
        int index = 0;
        for (Bind binding : program.bindings) {
            globalEnvironment.put(binding.variable, index++);
        }

        // Allocate the concrete value of a Bind at its address.
        for (Bind binding : program.bindings) {
            allocateGlobal(globalEnvironment, binding);
        }

        // Make immutable map as returned environment.
        return Collections.unmodifiableMap(mapValues(globalEnvironment, Address::new, HashMap::new));
    }

    private void allocateGlobal(final Map<Variable, Integer> globalEnvironment,
                                final Bind binding) {
        int address = globalEnvironment.get(binding.variable);

        List<Value> boundValues = new ArrayList<>();
        for (Variable freeVariable : binding.lambda.freeVariables) {
            boundValues.add(new Address(globalEnvironment.get(freeVariable)));
        }

        contents.put(address, new Closure(binding.lambda, boundValues));
    }
}
