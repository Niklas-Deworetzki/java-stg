package deworetzki.stg.syntax;

import deworetzki.parse.Position;

/**
 * A {@link Literal} describes a primitive integer value.
 */
public final class Literal extends Atom {
    public final int value;

    public Literal(Position position, int value) {
        super(position);
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%d#", value);
    }
}
