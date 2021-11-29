package deworetzki.messages;

import deworetzki.parse.Position;
import org.fusesource.jansi.Ansi;

import java.util.Optional;
import java.util.function.Predicate;

import static deworetzki.messages.MessageUtils.stringRepresentation;

public abstract class ErrorMessage extends RuntimeException implements CliMessage {
    private final Position position;
    private final Object expected, actual;

    // A sentinel value used to determine, whether an "expected" or "actual" value for the error message has been given.
    // Simply using null may lead to problems, whenever null is a desired or possible value.
    private static final Object NO_VALUE = new Object();

    protected ErrorMessage(String message, Position position, Object expected, Object actual) {
        super(message);
        this.position = position;
        this.expected = expected;
        this.actual = actual;
    }

    public ErrorMessage(String message, Position position) {
        this(message, position, NO_VALUE, NO_VALUE);
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

    public static class IllegalInputCharacter extends ErrorMessage {
        public IllegalInputCharacter(Position position, char offendingChar) {
            super(String.format("Detected illegal character %s in input.", stringRepresentation(offendingChar)),
                    position);
        }
    }

    public static class SyntaxError extends ErrorMessage {
        public SyntaxError(Position position, Iterable<String> expectedSymbols) {
            super("Syntax error detected.", position,
                    String.join(", ", expectedSymbols), NO_VALUE);
        }
    }
}