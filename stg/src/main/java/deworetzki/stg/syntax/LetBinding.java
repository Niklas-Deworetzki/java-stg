package deworetzki.stg.syntax;

import java.util.List;

public final class LetBinding extends Expression {
    public final List<Bind> bindings;
    public final boolean isRecursive;
    public final Expression expression;
}
