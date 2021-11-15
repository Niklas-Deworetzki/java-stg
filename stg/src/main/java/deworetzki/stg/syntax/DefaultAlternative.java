package deworetzki.stg.syntax;

import deworetzki.parse.Position;

/**
 * The default {@link Alternative} that matches all values without any action performed.
 */
public final class DefaultAlternative extends Alternative {
    public DefaultAlternative(Position position, Expression expression) {
        super(position, expression);
    }

    @Override
    public String toString() {
        return formatTree("Default");
    }
}
