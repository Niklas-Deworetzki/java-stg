package deworetzki.stg.syntax;

import deworetzki.parse.Position;

/**
 * An abstract superclass for <tt>case</tt> alternatives.
 * Alternatives are allowed to be:
 * <ul>
 *  <li>{@link AlgebraicAlternative algebraic alternatives}</li>
 *  <li>{@link PrimitiveAlternative primitive alternatives}</li>
 *  <li>{@link NameBindingAlternative default alternative binding a name}</li>
 *  <li>{@link DefaultAlternative default alternative without a name}</li>
 * </ul>
 */
public abstract class Alternative extends Node {
    public final Expression expression;

    public Alternative(Position position, Expression expression) {
        super(position);
        this.expression = expression;
    }
}
