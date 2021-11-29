package deworetzki.messages;

import deworetzki.parse.Position;
import org.fusesource.jansi.Ansi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static deworetzki.messages.MessageUtils.stringRepresentation;

public abstract class WarningMessage implements CliMessage {
    protected static List<WarningMessage> COLLECTED_WARNINGS = new ArrayList<>();

    private final String message;
    private final Position position;
    private final Object expected, actual;

    // A sentinel value used to determine, whether an "expected" or "actual" value for the error message has been given.
    // Simply using null may lead to problems, whenever null is a desired or possible value.
    private static final Object NO_VALUE = new Object();

    protected WarningMessage(String message, Position position, Object expected, Object actual) {
        this.message = message;
        this.position = position;
        this.expected = expected;
        this.actual = actual;
    }

    public WarningMessage(String message, Position position) {
        this(message, position, NO_VALUE, NO_VALUE);
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

    public void emitWarning() {
        COLLECTED_WARNINGS.add(this);
    }

    @Override
    public String toString() {
        return toText();
    }

    public static List<WarningMessage> flushWarnings() {
        List<WarningMessage> warnings = new ArrayList<>(COLLECTED_WARNINGS);
        COLLECTED_WARNINGS.clear();
        return warnings;
    }

    public static class IndentationCharacterMismatch extends WarningMessage {
        public IndentationCharacterMismatch(Position position, char previouslyUsed, char currentlyUsed) {
            super("Input mixes different types of indentation characters.", position,
                    stringRepresentation(previouslyUsed), stringRepresentation(currentlyUsed));
        }
    }
}