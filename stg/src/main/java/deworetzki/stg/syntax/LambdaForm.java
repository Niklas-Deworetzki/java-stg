package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

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
    public List<Variable> freeVariables;
    public final boolean isUpdateable;
    public final List<Variable> parameter;
    public final Expression body;

    public LambdaForm(Position position,
                      List<Variable> freeVariables, boolean isUpdateable, List<Variable> parameter, Expression body) {
        super(position);
        this.freeVariables = freeVariables;
        this.isUpdateable = isUpdateable;
        this.parameter = parameter;
        this.body = body;
    }

    public LambdaForm(List<Variable> freeVariables, boolean isUpdateable, List<Variable> parameter, Expression body) {
        this(Position.NONE, freeVariables, isUpdateable, parameter, body);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatTree("Lambda",
                formatMember("free", freeVariables),
                formatMember("update", isUpdateable ? "u" : "n"),
                formatMember("parameter", parameter),
                formatMember("body", body.toString()));
    }
}
