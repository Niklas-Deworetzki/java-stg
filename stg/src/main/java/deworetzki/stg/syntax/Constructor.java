package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

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
        if (obj instanceof Constructor constructor) return constructor.name.equals(this.name);
        return false;
    }

    public static boolean areEqual(Constructor c1, Constructor c2) {
        return c1.name.equals(c2.name); // TODO: Optimize?
    }
}
