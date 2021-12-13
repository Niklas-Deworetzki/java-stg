package deworetzki.stg.syntax;

/**
 * An interface used to mark {@link Atom valid atomic values}.
 * <p>
 * An {@link Atom} is either a {@link Variable} or a {@link Literal}.
 */
public sealed interface Atom permits Literal, Variable {
}
