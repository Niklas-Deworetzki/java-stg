package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

/**
 * A {@link DefaultAlternative} that matches any value and
 * binds the value to a {@link Variable name}.
 */
public final class DefaultBindingAlternative extends DefaultAlternative {
    public final Variable variable;

    public DefaultBindingAlternative(Position position, Variable variable, Expression expression) {
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
