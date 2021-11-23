package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

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
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("%d#", value);
    }
}
