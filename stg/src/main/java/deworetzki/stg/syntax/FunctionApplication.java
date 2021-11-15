package deworetzki.stg.syntax;

import deworetzki.parse.Position;

import java.util.List;

/**
 * The {@link Application} of a {@link Variable function bound to an explicit name}
 * to its {@link Atom arguments}.
 */
public final class FunctionApplication extends Application {
    public final Variable function;

    public FunctionApplication(Position position, Variable function, List<Atom> arguments) {
        super(position, arguments);
        this.function = function;
    }

    @Override
    public String toString() {
        return formatTree("FunctionApplication",
                formatMember("function", function.toString()),
                formatMember("arguments", arguments));
    }
}
