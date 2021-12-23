package deworetzki.stg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntBinaryOperator;
import java.util.function.ToIntFunction;

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

    public int getExpectedParameterCount() {
        return parameterCount;
    }

    public String getRepresentation() {
        return representation;
    }

    public PrimitiveFunction getFunction() {
        return function;
    }

    private static final Map<String, PrimitiveOperations> LOOKUP_MAP = new HashMap<>();

    static {
        for (PrimitiveOperations builtinOperation : values()) {
            LOOKUP_MAP.put(builtinOperation.getRepresentation(), builtinOperation);
        }
    }

    public static Optional<PrimitiveOperations> getBuiltin(String name) {
        return Optional.ofNullable(LOOKUP_MAP.get(name));
    }

    public static Set<String> definedBuiltins() {
        return LOOKUP_MAP.keySet();
    }


    @FunctionalInterface
    public interface PrimitiveFunction extends ToIntFunction<int[]> {
        static PrimitiveFunction fromBinaryOperator(IntBinaryOperator operator) {
            return (args) -> operator.applyAsInt(args[0], args[1]);
        }
    }
}
