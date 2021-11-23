package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

/**
 * The default {@link Alternative} that matches all values without any action performed.
 */
public final class DefaultAlternative extends Alternative<Alternative.Default> {
    public DefaultAlternative(Position position, Expression expression) {
        super(position, expression);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatTree("Default");
    }
}
