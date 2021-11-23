package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

import java.util.List;

/**
 * {@link CaseExpression case expressions} are used to force evaluation of a value.
 * <p>
 * The {@link CaseExpression#scrutinized scrutinized expression} is evaluated and
 * the first matching {@link Alternative} supplied for a result value.
 */
public final class CaseExpression extends Expression {
    public final Expression scrutinized;
    public final List<Alternative<? extends Alternative.NonDefault>> alternatives;
    public final Alternative<Alternative.Default> defaultAlternative;


    public CaseExpression(Position position, Expression scrutinized,
                          List<Alternative<? extends Alternative.NonDefault>> alternatives,
                          Alternative<Alternative.Default> defaultAlternative) {
        super(position);
        this.scrutinized = scrutinized;
        this.alternatives = alternatives;
        this.defaultAlternative = defaultAlternative;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatTree("case",
                formatMember("expression", scrutinized.toString()),
                formatMember("alternatives", alternatives),
                formatMember("default", defaultAlternative.toString()));
    }
}
