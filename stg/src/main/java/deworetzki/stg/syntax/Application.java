package deworetzki.stg.syntax;

import deworetzki.parse.Position;

import java.util.List;

/**
 * An abstract superclass of all {@link Application applications}.
 * <p>
 * It provides a {@link List} of {@link Atom arguments, which must be Atoms}.
 * This is ensured by either evaluating complex arguments or by binding them to
 * a closure on the heap using a {@link LetBinding let(rec) expression}.
 */
public abstract sealed class Application extends Expression permits ConstructorApplication, FunctionApplication, PrimitiveApplication {
    public final List<? extends Atom> arguments;

    public Application(Position position, List<? extends Atom> arguments) {
        super(position);
        this.arguments = arguments;
    }
}
