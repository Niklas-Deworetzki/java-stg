package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

import java.util.List;

/**
 * The {@link Application} of a {@link Variable function bound to an explicit name}
 * to its {@link Atom arguments}.
 */
public final class FunctionApplication extends Application {
    public final Variable function;

    public FunctionApplication(Position position, Variable function, List<? extends Atom> arguments) {
        super(position, arguments);
        this.function = function;
    }

    public FunctionApplication(Variable function, List<? extends Atom> arguments) {
        this(Position.NONE, function, arguments);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }


    @Override
    public String toString() {
        return formatTree("FunctionApplication",
                formatMember("function", function.toString()),
                formatMember("arguments", arguments));
    }
}
