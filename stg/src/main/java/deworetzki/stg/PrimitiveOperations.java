package deworetzki.stg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public enum PrimitiveOperations {
    ADD("+", 2),
    SUB("-", 2),
    DIV("/", 2),
    MUL("*", 2);

    private final String representation;
    private final int parameterCount;

    PrimitiveOperations(String representation, int parameterCount) {
        this.representation = representation + "#";
        this.parameterCount = parameterCount;
    }

    public int getExpectedParameterCount() {
        return parameterCount;
    }

    public String getRepresentation() {
        return representation;
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
}
