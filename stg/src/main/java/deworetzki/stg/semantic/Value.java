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

    /**
     * This function accepts an {@link Atom} and returns its {@link Value}.
     * <p>
     * It is implemented after <var>val</var> from <i>Implementing lazy functional languages
     * on stock hardware: the Spineless Tagless G-machine</i> by Simon Peyton Jones (p. 33).
     * <p>
     * A {@link Literal} is evaluated into its {@link Int} value. A {@link Variable} is looked
     * up in the surrounding environments, favoring local variables for global ones.
     *
     * <pre>
     * value &#963; &#961; k = Int k
     * value &#963; &#961; v = &#963; v     if v &#8712; dom(&#963;)
     *             = &#961; v     otherwise
     * </pre>
     *
     * @param localEnvironment  The local environment &#963;.
     * @param globalEnvironment The global environment &#961;.
     * @param atom              The {@link Atom} to evaluate.
     * @return The {@link Value} of an {@link Atom}.
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    static Value value(Map<Variable, Value> localEnvironment,
                       Map<Variable, Value> globalEnvironment,
                       Atom atom) {
        if (atom instanceof Literal literal) {
            return new Int(literal.value);
        } else if (localEnvironment.containsKey(atom)) {
            return localEnvironment.get(atom);
        } else {
            return globalEnvironment.get(atom);
        }
    }

    static List<Value> values(Map<Variable, Value> localEnvironment,
                              Map<Variable, Value> globalEnvironment,
                              List<? extends Atom> atoms) {
        return atoms.stream()
                .map(atom -> value(localEnvironment, globalEnvironment, atom))
                .collect(Collectors.toList());
    }
}
