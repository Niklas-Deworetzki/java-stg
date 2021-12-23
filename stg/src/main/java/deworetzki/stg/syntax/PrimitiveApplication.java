package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.PrimitiveOperations;
import deworetzki.stg.visitor.Visitor;

import java.util.List;

/**
 * The {@link Application} of a primitive operation to its {@link Atom arguments}.
 */
public final class PrimitiveApplication extends Application {
    public final String operation;

    public PrimitiveApplication(Position position, String operation, List<Atom> arguments) {
        super(position, arguments);
        this.operation = operation;
    }

    public PrimitiveOperations.PrimitiveFunction getOperation() {
        return PrimitiveOperations.getBuiltin(operation).orElseThrow().getFunction();
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatTree("PrimitiveApplication",
                formatMember("operation", operation),
                formatMember("arguments", arguments));
    }
}
