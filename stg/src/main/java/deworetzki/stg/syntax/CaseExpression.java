package deworetzki.stg.syntax;

import deworetzki.parse.Position;

import java.util.List;

/**
 * {@link CaseExpression case expressions} are used to force evaluation of a value.
 * <p>
 * The {@link CaseExpression#scrutinized scrutinized expression} is evaluated and
 * the first matching {@link Alternative} supplied for a result value.
 */
public final class CaseExpression extends Expression {
    public final Expression scrutinized;
    public final List<Alternative> alternatives;

    public CaseExpression(Position position, Expression scrutinized, List<Alternative> alternatives) {
        super(position);
        this.scrutinized = scrutinized;
        this.alternatives = alternatives;
    }

    @Override
    public String toString() {
        return formatTree("case",
                formatMember("expression", scrutinized.toString()),
                formatMember("alternatives", alternatives));
    }
}
