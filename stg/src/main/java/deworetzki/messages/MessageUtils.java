package deworetzki.messages;

public final class MessageUtils {
    private MessageUtils() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static String stringRepresentation(char character) {
        // Don't display whitespace and control characters, since they can mess with console output.
        if (Character.isWhitespace(character) || Character.isISOControl(character)) {
            return String.format("0x%X", (int) character);
        } else {
            return String.format("'%c'", character);
        }
    }
}
