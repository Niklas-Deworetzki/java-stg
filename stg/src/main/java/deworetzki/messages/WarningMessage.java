package deworetzki.messages;

import deworetzki.parse.Position;
import deworetzki.stg.Options;
import deworetzki.stg.syntax.Constructor;
import deworetzki.stg.syntax.LambdaForm;
import deworetzki.stg.syntax.Variable;
import org.fusesource.jansi.Ansi;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class WarningMessage implements CliMessage {
    private final String message;
    private final Position position;
    private Object expected = NO_VALUE, actual = NO_VALUE;
    private String hint;

    protected WarningMessage(Position position, String message, Object... args) {
        this.position = position;
        this.message = String.format(message, args);
    }

    protected void withExpected(Object expected) {
        this.expected = expected;
    }

    protected void withActual(Object actual) {
        this.actual = actual;
    }

    protected void withHint(String hint) {
        this.hint = hint;
    }

    @Override
    public String getTag() {
        return "Warning";
    }

    @Override
    public Ansi.Color getHighlightColor() {
        return Ansi.Color.MAGENTA;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    private static final Predicate<Object> hasValue = value -> value != NO_VALUE;

    @Override
    public Optional<Object> getExpected() {
        return Optional.of(expected).filter(hasValue);
    }

    @Override
    public Optional<Object> getActual() {
        return Optional.of(actual).filter(hasValue);
    }

    @Override
    public Optional<String> getHint() {
        return Optional.ofNullable(hint);
    }

    @Override
    public String toString() {
        return toText();
    }

    public static class AmbiguousBoxedInteger extends WarningMessage {
        public AmbiguousBoxedInteger(Position position) {
            super(position, "Ambiguous use of boxed literal in case. Did you mean to use a primitive or algebraic alternative?");
            withExpected("Algebraic or primitive alternative");
        }
    }

    public static class DoubleCaseArrow extends WarningMessage {
        public DoubleCaseArrow(Position position) {
            super(position, "Use of double arrow in case is not recommended. Cases are not updateable.");
            withExpected("Single arrow ( -> )");
            withActual("Double arrow ( => )");
        }
    }

    public static class NoImplicitFreeVariablesAllowed extends WarningMessage {
        public NoImplicitFreeVariablesAllowed(Position position) {
            super(position, "A free variable list is missing. Assuming no free variables are present, since inferring them is not allowed.");
            withExpected("A list of variables free in the lambda expression body.");
            withHint(Options.Extensions.INFER_FREE_VARIABLES.getHint());
        }
    }

    public static class UnnecessaryFreeVariables extends WarningMessage {
        public UnnecessaryFreeVariables(LambdaForm lambda, Set<Variable> unnecessary) {
            super(lambda.position, "Declared variables contain redundant entries.");
            withHint("You may remove the following variables: " +
                    unnecessary.stream().map(variable -> variable.name).collect(Collectors.joining(", ")));
        }
    }

    public static class ConstructorArgsDiffer extends WarningMessage {
        public ConstructorArgsDiffer(Constructor constructor, int argumentCount, Integer detectedCount) {
            super(constructor.position, "Constructor '%s' accepts %d parameters but earlier occurrences accepted %d.",
                    constructor.name, argumentCount, detectedCount);
            withExpected(detectedCount);
            withActual(argumentCount);
            withHint("It seems like '" + constructor.name + "' is used inconsistently.");
        }
    }
}