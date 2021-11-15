package deworetzki.stg.syntax;

import deworetzki.parse.Position;

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
    public String toString() {
        return formatTree("PrimitiveAlternative",
                formatMember("primitive", literal.toString()),
                formatMember("expression", expression.toString()));
    }
}
