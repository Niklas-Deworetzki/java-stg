package deworetzki.stg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntBinaryOperator;
import java.util.function.ToIntFunction;

/**
 * An enumeration of all primitive operations, available in this machine implementation.
 */
@SuppressWarnings("Convert2MethodRef")
public enum PrimitiveOperations {
    ADD("+", 2, (a, b) -> a + b),
    SUB("-", 2, (a, b) -> a - b),
    DIV("/", 2, (a, b) -> a / b),
    MUL("*", 2, (a, b) -> a * b);

    private final String representation;
    private final int parameterCount;
    private final PrimitiveFunction function;

    PrimitiveOperations(String representation, int parameterCount, IntBinaryOperator binaryOperator) {
        this.representation = representation + "#";
        this.parameterCount = parameterCount;
        this.function = PrimitiveFunction.fromBinaryOperator(binaryOperator);
    }

    /**
     * Returns a {@link String} that is used to refer to this operation in the STG language source code.
     */
    public String getRepresentation() {
        return representation;
    }

    /**
     * Returns the amount of parameters, that this operation expects.
     */
    public int getExpectedParameterCount() {
        return parameterCount;
    }

    /**
     * Returns a {@link PrimitiveFunction} which provides an implementation for this operation.
     */
    public PrimitiveFunction getFunction() {
        return function;
    }


    private static final Map<String, PrimitiveOperations> LOOKUP_MAP = new HashMap<>();

    static {
        // Built a fast lookup map, so we dont have to query values() every time, we want a builtin.
        for (PrimitiveOperations builtinOperation : values()) {
            LOOKUP_MAP.put(builtinOperation.getRepresentation(), builtinOperation);
        }
    }

    /**
     * Searches a builtin operation by name. Returns the searched builtin, if found.
     */
    public static Optional<PrimitiveOperations> getBuiltin(String name) {
        return Optional.ofNullable(LOOKUP_MAP.get(name));
    }

    /**
     * Returns a {@link Set} of names for all defined builtin operations.
     */
    public static Set<String> definedBuiltins() {
        return LOOKUP_MAP.keySet();
    }


    /**
     * General function, converting an arbitrary amount of <code>int</code> values
     * into a resulting <code>int</code>.
     * <p>
     * This is used to implement builtin operations on primitive values together with
     * {@link PrimitiveOperations} instances, which
     * {@link PrimitiveOperations#getExpectedParameterCount() specify the expected parameters}.
     */
    @FunctionalInterface
    public interface PrimitiveFunction extends ToIntFunction<int[]> {
        static PrimitiveFunction fromBinaryOperator(IntBinaryOperator operator) {
            return (args) -> operator.applyAsInt(args[0], args[1]);
        }
    }
}
