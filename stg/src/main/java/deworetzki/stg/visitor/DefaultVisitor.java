package deworetzki.stg.visitor;

import deworetzki.stg.syntax.*;

import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultVisitor<R> implements Visitor<R> {
    private final Function<Node, R> defaultFunction;

    public DefaultVisitor(final Function<Node, R> defaultFunction) {
        this.defaultFunction = defaultFunction;
    }

    public DefaultVisitor(final Supplier<R> defaultValueSupplier) {
        this((ignored) -> defaultValueSupplier.get());
    }

    public DefaultVisitor(final R defaultValue) {
        this((ignored) -> defaultValue);
    }

    public DefaultVisitor() {
        this((R) null);
    }


    @Override
    public R visit(AlgebraicAlternative alternative) {
        return defaultFunction.apply(alternative);
    }

    @Override
    public R visit(Alternatives alternatives) {
        return defaultFunction.apply(alternatives);
    }

    @Override
    public R visit(Bind bind) {
        return defaultFunction.apply(bind);
    }

    @Override
    public R visit(CaseExpression expression) {
        return defaultFunction.apply(expression);
    }

    @Override
    public R visit(Constructor constructor) {
        return defaultFunction.apply(constructor);
    }

    @Override
    public R visit(ConstructorApplication application) {
        return defaultFunction.apply(application);
    }

    @Override
    public R visit(DefaultBindingAlternative alternative) {
        return defaultFunction.apply(alternative);
    }

    @Override
    public R visit(DefaultFallthroughAlternative alternative) {
        return defaultFunction.apply(alternative);
    }

    @Override
    public R visit(FunctionApplication application) {
        return defaultFunction.apply(application);
    }

    @Override
    public R visit(LambdaForm lambda) {
        return defaultFunction.apply(lambda);
    }

    @Override
    public R visit(LetBinding let) {
        return defaultFunction.apply(let);
    }

    @Override
    public R visit(Literal literal) {
        return defaultFunction.apply(literal);
    }

    @Override
    public R visit(NoAlternative noAlternative) {
        return defaultFunction.apply(noAlternative);
    }

    @Override
    public R visit(PrimitiveAlternative alternative) {
        return defaultFunction.apply(alternative);
    }

    @Override
    public R visit(PrimitiveApplication application) {
        return defaultFunction.apply(application);
    }

    @Override
    public R visit(Program program) {
        return defaultFunction.apply(program);
    }

    @Override
    public R visit(Variable variable) {
        return defaultFunction.apply(variable);
    }
}
