package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

/**
 * {@link CaseExpression case expressions} are used to force evaluation of a value.
 * <p>
 * The {@link CaseExpression#scrutinized scrutinized expression} is evaluated and
 * the first matching {@link Alternative} supplied for a result value.
 */
public final class CaseExpression extends Expression {
    public final Expression scrutinized;
    public final Alternatives alternatives;

    public CaseExpression(Position position, Expression scrutinized, Alternatives alternatives) {
        super(position);
        this.scrutinized = scrutinized;
        this.alternatives = alternatives;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatTree("Case",
                formatMember("expression", scrutinized.toString()),
                formatMember("alternatives", alternatives.toString()));
    }
}
