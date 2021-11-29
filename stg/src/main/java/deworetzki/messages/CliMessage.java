package deworetzki.messages;

import deworetzki.parse.Position;
import org.fusesource.jansi.Ansi;

import java.util.Optional;

public interface CliMessage {

    String getTag();

    Ansi.Color getHighlightColor();


    String getMessage();

    Position getPosition();

    Optional<Object> getExpected();

    Optional<Object> getActual();

    default String toText() {
        final StringBuilder description = new StringBuilder();
        description.append(getMessage());

        getExpected().ifPresent(expected ->
                description.append("\n\tExpected: ").append(expected));

        getActual().ifPresent(actual ->
                description.append("\n\tActual: ").append(actual));

        if (getPosition() != null && getPosition() != Position.NONE) {
            description.append("\n")
                    .append(getPosition().toLongString());
        }

        return description.toString();
    }

    default void toAnsi(Ansi ansi) {
        ansi.fg(getHighlightColor()).bold()
                .a(getTag())
                .reset()
                .a(": ")
                .a(getMessage());

        getExpected().ifPresent(expected ->
                ansi.a("\n\tExpected: " + expected));

        getActual().ifPresent(actual ->
                ansi.a("\n\tActual:   " + actual));

        if (getPosition() != null && getPosition() != Position.NONE) {
            ansi.a("\n");
            getPosition().toAnsi(ansi);
        }
    }
}