package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

import java.util.List;

/**
 * Algebraic alternatives name an explicit {@link Constructor} as well as its arguments.
 * <p>
 * The alternative matches, if the value is of the same value and all arguments match.
 */
public final class AlgebraicAlternative extends Alternative<Alternative.Algebraic> {
    public final Constructor constructor;
    public final List<Atom> arguments;

    public AlgebraicAlternative(Position position, Constructor constructor, List<Atom> arguments, Expression expression) {
        super(position, expression);
        this.constructor = constructor;
        this.arguments = arguments;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatTree("Algebraic",
                formatMember("constructor", constructor.toString()),
                formatMember("arguments", arguments),
                formatMember("expression", expression.toString()));
    }
}
