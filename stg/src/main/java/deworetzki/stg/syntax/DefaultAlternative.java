package deworetzki.stg.syntax;

import deworetzki.parse.Position;

/**
 * Abstract superclass for the available {@link DefaultAlternative default alternatives}.
 */
public abstract class DefaultAlternative extends Alternative {
    public DefaultAlternative(Position position, Expression expression) {
        super(position, expression);
    }
}
