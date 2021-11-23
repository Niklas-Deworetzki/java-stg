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
public abstract class Alternative<V extends Alternative.Variant> extends Node {
    public final Expression expression;

    public Alternative(Position position, Expression expression) {
        super(position);
        this.expression = expression;
    }

    /**
     * Type tag root used to distinguish alternative variants
     * on a type system level.
     */
    interface Variant {
    }

    /**
     * Common root tag for alternatives that are not a
     * default alternative.
     */
    interface NonDefault extends Variant {
    }

    /**
     * Type tag for algebraic alternatives.
     */
    interface Algebraic extends NonDefault {
    }

    /**
     * Type tag for primitive alternatives.
     */
    interface Primitive extends NonDefault {
    }

    /**
     * Type tag for default alternatives.
     */
    interface Default extends Variant {
    }
}
