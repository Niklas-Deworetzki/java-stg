package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Variable is a name referring to a heap allocated closure.
 */
public final class Variable extends Node implements Atom {
    public final String name;

    public Variable(Position position, String name) {
        super(position);
        this.name = name;
    }

    public Variable(String name) {
        this(Position.NONE, name);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Variable var) {
            return this.name.equals(var.name);
        }
        return false;
    }

    public static List<Variable> arbitrary(int amount) {
        List<Variable> arbitraryVariables = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            arbitraryVariables.add(new Variable(freshName()));
        }
        return arbitraryVariables;
    }

    private static final AtomicInteger UNIQUE_VARIABLE_COUNTER = new AtomicInteger();
    private static final char START = '\u03B1', END = '\u03C9';

    private static String freshName() {
        int id = UNIQUE_VARIABLE_COUNTER.getAndIncrement();
        return String.format("%c%d", START + (id % (END - START)), id / (END - START));
    }
}
