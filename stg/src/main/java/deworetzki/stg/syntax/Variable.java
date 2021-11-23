package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

/**
 * A Variable is a name referring to a heap allocated closure.
 */
public final class Variable extends Atom {
    public final String name;

    public Variable(Position position, String name) {
        super(position);
        this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return name;
    }
}
