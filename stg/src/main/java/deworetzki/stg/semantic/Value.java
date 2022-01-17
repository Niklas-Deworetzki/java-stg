package deworetzki.stg.semantic;

import deworetzki.stg.syntax.Atom;
import deworetzki.stg.syntax.Literal;
import deworetzki.stg.syntax.Variable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public sealed interface Value {

    /**
     * An accessor returning the underlying <code>int</code> value
     * for a given concrete {@link Value} instance.
     */
    int getValue();


    /**
     * A heap address.
     */
    record Address(int address) implements Value {
        @Override
        public int getValue() {
            return address;
        }

        @Override
        public String toString() {
            return String.format("@%d", address);
        }
    }

    /**
     * A primitive integer value.
     */
    record Int(int value) implements Value {
        @Override
        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("%d#", value);
        }
    }

    // TODO: Documentation from val
    static Value value(Map<Variable, Value> localEnvironment,
                       Map<Variable, Value> globalEnvironment,
                       Atom atom) {
        if (atom instanceof Literal literal) {
            return new Int(literal.value);
        } else {
            return getValue(localEnvironment, globalEnvironment, (Variable) atom);
        }
    }

    static List<Value> values(Map<Variable, Value> localEnvironment,
                              Map<Variable, Value> globalEnvironment,
                              List<? extends Atom> atoms) {
        return atoms.stream()
                .map(atom -> value(localEnvironment, globalEnvironment, atom))
                .collect(Collectors.toList());
    }

    private static <K, V> V getValue(Map<K, V> local, Map<K, V> global, K key) {
        final V result = local.get(key);
        return (result != null) ? result : global.get(key);
    }
}
