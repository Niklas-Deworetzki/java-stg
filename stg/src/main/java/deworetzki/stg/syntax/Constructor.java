package deworetzki.stg.syntax;

import deworetzki.parse.Position;

/**
 * This class encapsulates {@link Constructor} values.
 * <p>
 * Constructors are either used in {@link ConstructorApplication}
 * to create data type values or in {@link AlgebraicAlternative}
 * to take apart these values.
 */
public final class Constructor extends Node {
    public final String name;

    public Constructor(Position position, String name) {
        super(position);
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
