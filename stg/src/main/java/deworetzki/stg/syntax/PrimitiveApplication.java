package deworetzki.stg.syntax;

import deworetzki.parse.Position;

import java.util.List;

/**
 * The {@link Application} of a primitive operation to its {@link Atom arguments}.
 */
public final class PrimitiveApplication extends Application {
    public final String operation; // TODO: Enum for allowed operators?

    public PrimitiveApplication(Position position, String operation, List<Atom> arguments) {
        super(position, arguments);
        this.operation = operation;
    }

    @Override
    public String toString() {
        return formatTree("PrimitiveApplication",
                formatMember("operation", operation),
                formatMember("arguments", arguments));
    }
}
