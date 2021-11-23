package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

/**
 * A {@link Bind} is used to define a {@link Variable name} for a {@link LambdaForm}.
 */
public final class Bind extends Node {
    public final Variable variable;
    public final LambdaForm lambda;

    public Bind(Position position, Variable variable, LambdaForm lambda) {
        super(position);
        this.variable = variable;
        this.lambda = lambda;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatTree("Bind",
                formatMember("name", variable.toString()),
                formatMember("lambda", lambda.toString()));
    }
}
