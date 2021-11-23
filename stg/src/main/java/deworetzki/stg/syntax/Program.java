package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

import java.util.List;

/**
 * A {@link Program} holds all top level {@link Bind bindings}.
 */
public final class Program extends Node {
    public final List<Bind> bindings;

    public Program(Position position, List<Bind> bindings) {
        super(position);
        this.bindings = bindings;
    }


    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatTree("Program", bindings);
    }

}
