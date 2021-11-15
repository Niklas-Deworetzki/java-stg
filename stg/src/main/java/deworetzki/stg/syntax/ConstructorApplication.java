package deworetzki.stg.syntax;

import deworetzki.parse.Position;

import java.util.List;

/**
 * The {@link Application} of a {@link Constructor} to its {@link Atom arguments}.
 * <p>
 * Constructor applications must always be saturated. This means that all required
 * arguments must be supplied.
 */
public final class ConstructorApplication extends Application {
    public final Constructor constructor;

    public ConstructorApplication(Position position, Constructor constructor, List<Atom> arguments) {
        super(position, arguments);
        this.constructor = constructor;
    }

    @Override
    public String toString() {
        return formatTree("ConstructorApplication",
                formatMember("constructor", constructor.toString()),
                formatMember("arguments", arguments));
    }
}
