package deworetzki.messages;

import deworetzki.parse.Position;
import deworetzki.stg.Options;
import org.fusesource.jansi.Ansi;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

import static deworetzki.messages.MessageUtils.stringRepresentation;

public abstract class ErrorMessage extends RuntimeException implements CliMessage {
    private final Position position;
    private Object expected = NO_VALUE, actual = NO_VALUE;
    private String hint;

    // A sentinel value used to determine, whether an "expected" or "actual" value for the error message has been given.
    // Simply using null may lead to problems, whenever null is a desired or possible value.
    private static final Object NO_VALUE = new Object();

    protected ErrorMessage(String message, Position position) {
        super(message);
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
            super(message, Position.NONE);
            initCause(cause);
        }

        public InternalError(String message) {
            super(message, Position.NONE);
        }
    }

    public static class InputError extends ErrorMessage {
        public InputError(IOException exception) {
            super(exception.getMessage() != null ? exception.getMessage() : exception.toString(), Position.NONE);
            addSuppressed(exception);
        }
    }

    public static class IllegalInputCharacter extends ErrorMessage {
        public IllegalInputCharacter(Position position, char offendingChar) {
            super(String.format("Detected illegal character %s in input.", stringRepresentation(offendingChar)),
                    position);
        }
    }

    public static class InvalidNumber extends ErrorMessage {
        public InvalidNumber(Position position, String number) {
            super(String.format("Invalid number literal '%s' detected in input.", number), position);
        }
    }

    public static class BoxedLiteral extends ErrorMessage {
        public BoxedLiteral(Position position) {
            super("Boxed literals are not allowed.", position);
            withHint(Options.Extensions.ALLOW_NONPRIMITIVE_NUMBERS.getHint());
        }
    }

    public static class SyntaxError extends ErrorMessage {
        public SyntaxError(Position position, Iterable<String> expectedSymbols) {
            super("Syntax error detected.", position);
            withExpected(String.join(", ", expectedSymbols));
        }
    }
}