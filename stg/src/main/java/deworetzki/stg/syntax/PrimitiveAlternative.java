package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

/**
 * A primitive alternative matches only if the scrutinized value
 * equals the {@link Literal} provided.
 */
public final class PrimitiveAlternative extends Alternative {
    public final Literal literal;

    public PrimitiveAlternative(Position position, Literal literal, Expression expression) {
        super(position, expression);
        this.literal = literal;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatTree("PrimitiveAlternative",
                formatMember("primitive", literal.toString()),
                formatMember("expression", expression.toString()));
    }
}
