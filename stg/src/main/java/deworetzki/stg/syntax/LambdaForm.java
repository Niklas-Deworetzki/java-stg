package deworetzki.stg.syntax;

import deworetzki.parse.Position;

import java.util.List;

/**
 * {@link LambdaForm LambdaForms} provide a special form of binding,
 * resembling regular lambda expressions.
 * <p>
 * In the STG-Language, the {@link LambdaForm#freeVariables free variables}
 * of a closure are made explicit and an {@link LambdaForm#isUpdateable update flag}
 * must be provided as well.
 */
public final class LambdaForm extends Node {
    public final List<Variable> freeVariables;
    public final boolean isUpdateable;
    public final List<Variable> arguments;
    public final Expression body;

    public LambdaForm(Position position,
                      List<Variable> freeVariables, boolean isUpdateable, List<Variable> arguments, Expression body) {
        super(position);
        this.freeVariables = freeVariables;
        this.isUpdateable = isUpdateable;
        this.arguments = arguments;
        this.body = body;
    }

    @Override
    public String toString() {
        return formatTree("Lambda",
                formatMember("free", freeVariables),
                formatMember("update", isUpdateable ? "u" : "n"),
                formatMember("arguments", arguments),
                formatMember("body", body.toString()));
    }
}
