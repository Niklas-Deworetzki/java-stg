package deworetzki.parse;

import org.fusesource.jansi.Ansi;

import java.util.Optional;

/**
 * This class represents a position in a file or input stream. A {@link Position} has information about the line and
 * column in a given {@link Source} where it is located.
 * <p>
 * Whenever a {@link Position} has to be provided and no valid {@link Position} is present, {@link Position#NONE} may
 * be used to represent the absence of a valid {@link Position}.
 * </p>
 * <p>
 * A {@link Position} instance has accessor methods {@link Position#getLine()}, {@link Position#getColumn()} and
 * {@link Position#getSource()} to request details of the coordinates in a {@link Source} represented by this instance.
 * Additionally the line containing this {@link Position} may be obtained using {@link Position#getSourceLine()}.
 * This method however may not always return a value.
 * </p>
 * <p>
 * For simple usage the methods {@link Position#toString()} and {@link Position#toLongString()} may be used, which
 * give a textual representation of an instance. The simpler {@link Position#toString()} method prints the name
 * of the containing source as well as line and column values.
 * <pre>
 * /home/user/file.txt: Line 12, column 4
 * </pre>
 * The more advanced {@link Position#toLongString()} method tries to additionally display the source's line containing
 * this {@link Position}.
 * <pre>
 * /home/user/file.txt: Line 12, column 4
 *   12 | This is an example line.
 *      |    ^
 * </pre>
 * This method has a variant {@link Position#toAnsi(Ansi, Ansi.Color)} that uses the <i>jansi</i> library to color
 * the output of {@link Position#toLongString()}.
 * </p>
 */
public record Position(Source source, int line, int column) {
    public static final Position NONE = new Position(null, -1, -1);

    /**
     * Creates a new {@link Position} instance using the given {@link Source}, line and column to represent
     * coordinates in the source.
     *
     * @param source The {@link Source} containing this position.
     * @param line   The line in the {@link Source}.
     * @param column The column in the {@link Source}.
     */
    public Position {
    }

    /**
     * Returns the column index of this {@link Position}.
     *
     * @return The column index of this {@link Position}.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the line index of this {@link Position}.
     *
     * @return the line index of this {@link Position}.
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the {@link Source} that contains this {@link Position}.
     *
     * @return The {@link Source} containing this {@link Position}.
     */
    public Source getSource() {
        return source;
    }

    /**
     * Tries to get the textual line containing this {@link Position} from its {@link Source}.
     * <p>
     * The value returned by this method depends on the ability of the {@link Source} to acquire the original line
     * of input. If it is possible to acquire the line again, it is returned. Otherwise the return value is empty.
     * </p>
     *
     * @return The textual line containing this {@link Position} if possible. Returns an empty result otherwise.
     */
    public Optional<String> getSourceLine() {
        if (source == null) return Optional.empty();
        return source.getLine(line);
    }

    /**
     * Creates a simple textual representation of this {@link Position}.
     * <p>
     * The returned {@link String} contains the containing {@link Source}'s name followed by the line and the column
     * as decimal numbers.
     * </p>
     * <p>
     * Example output:
     * <pre>
     * /home/user/file.txt: Line 12, column 4
     * </pre>
     * </p>
     *
     * @return A simple {@link String} representation of this {@link Position}.
     */
    public String toString() {
        if (source == null) return String.format("<unknown source>: Line %d, column %d", getLine(), getColumn());
        return String.format("%s: Line %d, column %d", source.getName(), getLine(), getColumn());
    }

    private static String createSourceLinePrefix(int line) {
        return String.format("  %d | ", line);
    }

    private static String createSourceLineEmptyPrefix(int line) {
        return createSourceLinePrefix(line).replaceAll("[0-9]", " ");
    }

    /**
     * This method creates an string that is <code>column</code> characters long. The last character in this string
     * is the <code>^</code> (circumflex) character, used as an arrow pointing to the column in the line above.
     *
     * @param source The source line is needed to match the indentation of the line, where the arrow should point to.
     *               Every character in this line is assumed to have the width of one space except for TAB characters,
     *               which are represented by a TAB character.
     * @param column The column which the arrow should point at.
     */
    private static String createArrowString(String source, int column) {
        char[] buffer = new char[column];

        // Pad the arrow according to the source line.
        for (int i = 0; i < column - 1; i++) {
            if (source.charAt(i) == '\t') {
                buffer[i] = '\t';
            } else {
                buffer[i] = ' ';
            }
        }
        // Finally place the arrow marker below the source character.
        buffer[column - 1] = '^';
        return String.valueOf(buffer);
    }

    /**
     * Creates a more detailed representation of this {@link Position}.
     * <p>
     * Additionally to the normal representation obtained via {@link Position#toString()}, this method tries
     * to append the source line containing this {@link Position}. If the method {@link Position#getSourceLine()} does
     * not return a value, the value returned by this method is equal to {@link Position#toString()}.
     * </p>
     * <p>
     * Example output:
     * <pre>
     * /home/user/file.txt: Line 12, column 4
     *   12 | This is an example line.
     *      |    ^
     * </pre>
     * </p>
     *
     * @return A detailed {@link String} representation of this instance.
     */
    public String toLongString() {
        Optional<String> sourceLine = getSourceLine();

        if (sourceLine.isEmpty()) {
            return toString();
        }

        return String.join("\n",
                toString(),
                createSourceLinePrefix(line) + sourceLine.get(),
                createSourceLineEmptyPrefix(line) + createArrowString(sourceLine.get(), column));
    }

    /**
     * The output written to the {@link Ansi} object by this method is structurally equivalent to the output of
     * {@link Position#toLongString()}. It additionally adds color to the output to visually separate different
     * parts of information.
     * <p>
     * Specifically the {@link Source}'s name is printed bold in front of the line and column information. The prefix
     * strings prepended to the source line and the arrow showing the position is highlighted in cyan. The arrow
     * highlighting the column itself is printed in bold red for better visibility.
     * </p>
     *
     * @param ansi                An {@link Ansi} buffer where the output is written to.
     * @param arrowHighlightColor The color used to highlight the error describing the error position.
     */
    public void toAnsi(Ansi ansi, Ansi.Color arrowHighlightColor) {
        ansi.bold()
                .a(source.getName())
                .a(": ").boldOff();
        ansi.a("Line ").a(line)
                .a(", column ").a(column);

        Optional<String> sourceLine = getSourceLine();

        if (sourceLine.isPresent()) {
            ansi.a("\n");
            ansi.fg(Ansi.Color.CYAN)
                    .a(createSourceLinePrefix(line))
                    .reset();
            ansi.a(sourceLine.get()).
                    a("\n");
            ansi.fg(Ansi.Color.CYAN)
                    .a(createSourceLineEmptyPrefix(line))
                    .reset();
            ansi.fg(arrowHighlightColor).bold()
                    .a(createArrowString(sourceLine.get(), column))
                    .reset();
        }
    }
}
