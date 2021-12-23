package deworetzki.messages;

import deworetzki.parse.Position;
import deworetzki.stg.Options;
import deworetzki.stg.semantic.Code;
import deworetzki.stg.syntax.*;
import org.fusesource.jansi.Ansi;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static deworetzki.messages.MessageUtils.stringRepresentation;

public abstract class ErrorMessage extends RuntimeException implements CliMessage {
    private final Position position;
    private Object expected = NO_VALUE, actual = NO_VALUE;
    private String hint;

    protected ErrorMessage(Position position, String message, Object... args) {
        super(String.format(message, args));
        this.position = position;
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
        return "Error";
    }

    @Override
    public Ansi.Color getHighlightColor() {
        return Ansi.Color.RED;
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


    public static class InternalError extends ErrorMessage {
        public InternalError(String message, Throwable cause) {
            super(Position.NONE, message);
            initCause(cause);
        }

        public InternalError(String message) {
            super(Position.NONE, message);
        }

        public InternalError(Position position, String message) {
            super(position, message);
        }
    }

    public static class InputError extends ErrorMessage {
        public InputError(IOException exception) {
            super(Position.NONE, exception.getMessage() != null ? exception.getMessage() : exception.toString());
            addSuppressed(exception);
        }
    }

    public static class IllegalInputCharacter extends ErrorMessage {
        public IllegalInputCharacter(Position position, char offendingChar) {
            super(position, "Detected illegal character %s in input.", stringRepresentation(offendingChar));
        }
    }

    public static class InvalidNumber extends ErrorMessage {
        public InvalidNumber(Position position, String number) {
            super(position, "Invalid number literal '%s' detected in input.", number);
        }
    }

    public static class BoxedLiteral extends ErrorMessage {
        public BoxedLiteral(Position position) {
            super(position, "Boxed literals are not allowed.");
            withHint(Options.Extensions.ALLOW_NONPRIMITIVE_NUMBERS.getHint());
        }
    }

    public static class ExpressionWithoutLambda extends ErrorMessage {
        public ExpressionWithoutLambda(Position position) {
            super(position, "Found an expression where a lambda was expected.");
            withExpected("\\ { free variables } par1 ... parN -> Expression");
            withHint(Options.Extensions.EXPRESSION_AS_LAMBDA.getHint());
        }
    }

    public static class SyntaxError extends ErrorMessage {
        public SyntaxError(Position position, Collection<String> expectedSymbols) {
            super(position, "Syntax error detected.");
            if (!expectedSymbols.isEmpty()) {
                withExpected(String.join(", ", expectedSymbols));
            }
        }
    }


    public static class Redeclaration extends ErrorMessage {
        public Redeclaration(Variable variable) {
            super(variable.position, "Redeclaration of variable '%s' is not allowed here.", variable.name);
        }
    }

    public static class UndeclaredFreeVariables extends ErrorMessage {
        public UndeclaredFreeVariables(LambdaForm lambda, Set<Variable> freeVariables) {
            super(lambda.position, "Declared list of free variables is incomplete.");
            withHint("The following variables are free in the given lambda: " +
                    freeVariables.stream().map(variable -> variable.name).collect(Collectors.joining(", ")));
        }
    }

    public static class UnknownVariable extends ErrorMessage {
        public UnknownVariable(Variable variable, Stream<Variable> scope) {
            super(variable.position, "Unknown variable '%s' encountered.", variable.name);
            // TODO: Add hint with similar name?
        }
    }

    public static class MainMissing extends ErrorMessage {
        public MainMissing() {
            super(Position.NONE, "No entry point is defined for this program!");
            withHint("Add a global definition 'main' without parameters.");
        }
    }

    public static class MainWithParameters extends ErrorMessage {
        public MainWithParameters(LambdaForm mainLambda) {
            super(mainLambda.position, "Lambda form bound to main is not allowed to have parameters!");
            withExpected("No parameters for lambda form.");
            withActual(mainLambda.parameters.size() + " declared parameters.");
        }
    }

    public static class UnknownPrimitive extends ErrorMessage {
        public UnknownPrimitive(PrimitiveApplication application) {
            super(application.position, "Unknown primitive '%s' is called.", application.operation);
            // TODO: Add hint with similar name?
        }
    }

    public static class ParameterMismatch extends ErrorMessage {
        public ParameterMismatch(Application application, int expectedParameterCount) {
            super(application.position, "Application uses a wrong amount of parameters.");
            withExpected(expectedParameterCount);
            withActual(application.arguments.size());
        }
    }


    public static class NoMatchingAlternative extends ErrorMessage {
        public NoMatchingAlternative(Alternatives alternatives, Code.ReturnConstructor ret) {
            super(alternatives.position, "No alternative matches %s(%s).", ret.constructor(),
                    ret.arguments().stream().map(String::valueOf).collect(Collectors.joining(" ")));
        }

        public NoMatchingAlternative(Alternatives alternatives, Code.ReturnInteger ret) {
            super(alternatives.position, "No alternative matches primitive %d.", ret.integer());
        }
    }

    public static class MixingAlternativeVariants extends ErrorMessage {
        public MixingAlternativeVariants(Alternatives alternatives) {
            super(alternatives.position, "Case expression combines algebraic and primitive alternatives.");
        }
    }
}