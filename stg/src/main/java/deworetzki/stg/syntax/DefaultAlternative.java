package deworetzki.stg.syntax;

import deworetzki.parse.Position;

/**
 * Abstract superclass for the available {@link DefaultAlternative default alternatives}.
 */
public abstract sealed class DefaultAlternative extends Alternative permits DefaultBindingAlternative, DefaultFallthroughAlternative, NoAlternative {
    public DefaultAlternative(Position position, Expression expression) {
        super(position, expression);
    }
}
