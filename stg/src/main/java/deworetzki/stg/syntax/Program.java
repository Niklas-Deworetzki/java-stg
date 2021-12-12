package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

import java.util.Iterator;
import java.util.List;

/**
 * A {@link Program} holds all top level {@link Bind bindings}.
 */
public final class Program extends Node implements Iterable<Bind> {
    public final List<Bind> bindings;

    public Program(List<Bind> bindings) {
        super(Position.NONE);
        this.bindings = bindings;
    }

    @Override
    public Iterator<Bind> iterator() {
        return bindings.iterator();
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
