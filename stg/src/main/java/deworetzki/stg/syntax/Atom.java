package deworetzki.stg.syntax;

import deworetzki.parse.Position;

/**
 * An abstract superclass of {@link Atom valid atomic values}.
 * <p>
 * An {@link Atom} is either a {@link Variable} or a {@link Literal}.
 */
public abstract class Atom extends Expression {
    public Atom(Position position) {
        super(position);
    }
}
