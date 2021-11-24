package deworetzki.stg.visitor;

import deworetzki.stg.syntax.*;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Visitor<R> extends Consumer<Node>, Function<Node, R> {
    @Override
    default void accept(Node node) {
        node.accept(this);
    }

    @Override
    default R apply(Node node) {
        return node.accept(this);
    }

    R visit(AlgebraicAlternative alternative);

    R visit(Alternatives alternatives);

    R visit(Bind bind);

    R visit(CaseExpression expression);

    R visit(Constructor constructor);

    R visit(ConstructorApplication application);

    R visit(DefaultFallthroughAlternative alternative);

    R visit(FunctionApplication application);

    R visit(LambdaForm lambda);

    R visit(LetBinding let);

    R visit(Literal literal);

    R visit(DefaultBindingAlternative alternative);

    R visit(PrimitiveAlternative alternative);

    R visit(PrimitiveApplication application);

    R visit(Program program);

    R visit(Variable variable);
}
