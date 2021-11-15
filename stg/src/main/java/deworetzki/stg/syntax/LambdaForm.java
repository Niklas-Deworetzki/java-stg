package deworetzki.stg.syntax;

import java.util.List;

public final class LambdaForm extends Node {
    public final List<Variable> freeVariables;
    public final boolean isUpdateable;
    public final List<Variable> arguments;
    public final Expression body;
}
