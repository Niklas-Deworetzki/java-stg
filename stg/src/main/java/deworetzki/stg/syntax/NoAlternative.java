package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

/**
 * {@link DefaultAlternative} used to signal that no {@link DefaultAlternative}
 * was provided.
 */
public final class NoAlternative extends DefaultAlternative {
    public NoAlternative(Position position) {
        super(position, null);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatTree("NoAlternative");
    }
}
