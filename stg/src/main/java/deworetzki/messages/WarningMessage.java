package deworetzki.messages;

import deworetzki.parse.Position;
import org.fusesource.jansi.Ansi;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class WarningMessage implements CliMessage {
    private final String message;
    private final Position position;
    private Object expected = NO_VALUE, actual = NO_VALUE;
    private String hint;

    // A sentinel value used to determine, whether an "expected" or "actual" value for the error message has been given.
    // Simply using null may lead to problems, whenever null is a desired or possible value.
    private static final Object NO_VALUE = new Object();

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
            super(position, "Ambiguous use of boxed literal in case. Did you meant to use a primitive or algebraic alternative?");
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
}