package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

import java.util.List;

/**
 * The {@link Application} of a {@link Constructor} to its {@link Atom arguments}.
 * <p>
 * Constructor applications must always be saturated. This means that all required
 * arguments must be supplied.
 */
public final class ConstructorApplication extends Application {
    public final Constructor constructor;

    public ConstructorApplication(Position position, Constructor constructor, List<? extends Atom> arguments) {
        super(position, arguments);
        this.constructor = constructor;
    }

    public ConstructorApplication(Constructor constructor, List<? extends Atom> arguments) {
        this(Position.NONE, constructor, arguments);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatTree("ConstructorApplication",
                formatMember("constructor", constructor.toString()),
                formatMember("arguments", arguments));
    }
}
