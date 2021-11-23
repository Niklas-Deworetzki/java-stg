package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

import java.util.List;

/**
 * {@link LetBinding let and letrec} expressions bind a name to a
 * heap allocated closure.
 */
public final class LetBinding extends Expression {
    public final boolean isRecursive;
    public final List<Bind> bindings;
    public final Expression expression;

    public LetBinding(Position position, boolean isRecursive, List<Bind> bindings, Expression expression) {
        super(position);
        this.isRecursive = isRecursive;
        this.bindings = bindings;
        this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatTree("Let",
                formatMember("recursive", Boolean.toString(isRecursive)),
                formatMember("bindings", bindings),
                formatMember("expression", expression.toString()));
    }
}
