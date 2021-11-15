package deworetzki.parse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * This class represents the source of an input. Currently supported inputs are a {@link File} or the
 * standard input stream.
 * <p>
 * A {@link Source} instance is acquired by either calling {@link Source#fromFile(File)} passing a {@link File} object
 * as input or calling {@link Source#fromStdIn()} to create a {@link Source} for the standard input stream.
 * </p>
 * <p>
 * Every {@link Source} has a name, that is accessible via {@link Source#getName()}. It is also possible to request
 * a line within the source. This however may fail, since the {@link File} originally used to create the {@link Source}
 * may have been altered at the moment of call, or there wasn't a {@link File} provided as the {@link Source}
 * can represent an input stream.
 * </p>
 */
public final class Source {
    private final File inputFile;
    private final String name;

    private Source(File inputFile, String name) {
        this.inputFile = inputFile;
        this.name = name;
    }

    /**
     * Creates a new {@link Source} for the given {@link File}.
     */
    public static Source fromFile(File file) {
        return new Source(file, file.getPath());
    }

    /**
     * Creates a new {@link Source} for the standard input stream.
     */
    public static Source fromStdIn() {
        return new Source(null, "<stdin>");
    }

    /**
     * Returns the name of this {@link Source}.
     *
     * @return The name of this {@link Source}.
     */
    public String getName() {
        return name;
    }

    /**
     * Tries to return the textual line represented by the given line number in the input
     * described by this {@link Source}.
     * <p>
     * If the {@link File} used to create this source was altered, this method may fail returning an empty result.
     * It is also possible, that this {@link Source} describes an input stream, in which case the result will be
     * empty too.
     * </p>
     *
     * @param lineNumber The number of the line to be fetched from the {@link File} described by this {@link Source}.
     *                   Line numbers start counting from one.
     * @return The textual line if possible, empty otherwise.
     */
    public Optional<String> getLine(int lineNumber) {
        if (inputFile == null) {
            return Optional.empty();
        }

        try (var linesStream = Files.lines(inputFile.toPath())) {
            Iterator<String> lines = linesStream.iterator();

            while (lineNumber-- > 1) {
                lines.next();
            }
            return Optional.of(lines.next());
        } catch (IOException | NoSuchElementException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "Source{" + name + '}';
    }
}
