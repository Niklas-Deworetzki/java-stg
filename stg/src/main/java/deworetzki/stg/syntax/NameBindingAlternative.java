package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

/**
 * A default {@link Alternative} that matches any value but
 * binds the value to a {@link Variable name}.
 */
public final class NameBindingAlternative extends Alternative<Alternative.Default> {
    public final Variable variable;

    public NameBindingAlternative(Position position, Variable variable, Expression expression) {
        super(position, expression);
        this.variable = variable;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatTree("BindingAlternative",
                formatMember("variable", variable.toString()),
                formatMember("expression", expression.toString()));
    }
}
