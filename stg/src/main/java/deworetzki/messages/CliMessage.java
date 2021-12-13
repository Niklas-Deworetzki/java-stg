package deworetzki.messages;

import deworetzki.parse.Position;
import org.fusesource.jansi.Ansi;

import java.util.Optional;

public interface CliMessage {

    // A sentinel value used to determine, whether an "expected" or "actual" value for the error message has been given.
    // Simply using null may lead to problems, whenever null is a desired or possible value.
    Object NO_VALUE = new Object();

    String getTag();

    Ansi.Color getHighlightColor();


    String getMessage();

    Position getPosition();

    Optional<Object> getExpected();

    Optional<Object> getActual();

    Optional<String> getHint();

    default void report() {
        System.out.println(toAnsi());
    }

    default String toText() {
        final StringBuilder description = new StringBuilder();
        description.append(getMessage());

        getExpected().ifPresent(expected ->
                description.append("\n\tExpected: ").append(expected));

        getActual().ifPresent(actual ->
                description.append("\n\tActual: ").append(actual));

        getHint().ifPresent(hint ->
                description.append("\n\tHint: ").append(hint));

        if (getPosition() != null && getPosition() != Position.NONE) {
            description.append("\n")
                    .append(getPosition().toLongString());
        }

        return description.toString();
    }

    default Ansi toAnsi() {
        final Ansi ansi = Ansi.ansi();
        ansi.fg(getHighlightColor()).bold()
                .a(getTag())
                .reset()
                .a(": ")
                .a(getMessage());

        getExpected().ifPresent(expected ->
                ansi.a("\n\tExpected: " + expected));

        getActual().ifPresent(actual ->
                ansi.a("\n\tActual:   " + actual));

        getHint().ifPresent(hint ->
                ansi.append("\n\tHint: ").append(hint));

        if (getPosition() != null && getPosition() != Position.NONE) {
            ansi.a("\n");
            getPosition().toAnsi(ansi, getHighlightColor());
        }
        return ansi;
    }
}