package deworetzki.stg;

import deworetzik.stg.parse.Parser;
import deworetzki.messages.ErrorMessage;
import deworetzki.parse.Source;
import deworetzki.parse.symbol.RichSymbolFactory;
import deworetzki.stg.parser.Scanner;
import deworetzki.stg.syntax.Bind;
import deworetzki.stg.syntax.Program;
import deworetzki.utils.ResourceProvider;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * The entry point of this program.
 * <p>
 * Parse command line arguments, load input programs, and pass them to the {@link StgRuntime}.
 */
public final class Main {

    public static void main(String[] args) {
        final Options options = new Options();
        try {
            // Try to parse command line options.
            CommandLine.populateCommand(options, args);
        } catch (CommandLine.PicocliException ignored) {
            // Picocli might report errors, in which case we will print a help message.
            options.isHelpRequested = true;
        }

        if (options.shouldDisplayHelp()) {
            CommandLine.usage(options, System.out);
        } else {
            try {
                loadProgram(options).ifPresent(new StgRuntime(options)::analyzeAndRun);
            } catch (ErrorMessage errorMessage) {
                System.out.println(errorMessage.toAnsi());
            }
        }
    }

    /**
     * Tries to load all input files specified, combining them into a single {@link Program}.
     * <p>
     * If any error occurs during loading, this method will return {@link Optional#empty() no result}.
     */
    private static Optional<Program> loadProgram(final Options options) {
        final List<Bind> inputProgram = new ArrayList<>();

        if (options.loadPrelude()) {
            try {
                inputProgram.addAll(loadPrelude(options));
            } catch (Exception failedToLoadPrelude) {
                // If the prelude fails to load, we probably shouldn't try to progress.
                new ErrorMessage.InternalError("Failed to load prelude: " +
                        failedToLoadPrelude.getMessage()).report();
                return Optional.empty();
            }
        }

        boolean anyFailed = false;
        for (var inputFile : options) {
            try {
                inputProgram.addAll(loadInput(options, inputFile));
            } catch (ErrorMessage errorMessage) {
                anyFailed = true;
                errorMessage.report();
            }
        }

        if (anyFailed) return Optional.empty();
        return Optional.of(new Program(inputProgram));
    }

    /**
     * Load the Prelude.
     */
    private static List<Bind> loadPrelude(final Options options) throws ErrorMessage {
        if (!options.loadPrelude()) return Collections.emptyList();
        return loadInput(options, () -> new Source(null, "<builtin prelude>",
                Main.class.getResourceAsStream("/Prelude.stg"), true));
    }

    /**
     * Load a {@link Source} provided by the given {@link ResourceProvider}.
     * <p>
     * This configures {@link Scanner} and {@link Parser} with the given {@link Options},
     * to run them on the input available under the {@link Source}.
     */
    private static List<Bind> loadInput(final Options options,
                                        final ResourceProvider<Source, IOException> resource) throws ErrorMessage {
        try (Source source = resource.get()) {
            final Scanner lexer = new Scanner(new InputStreamReader(source.getInputStream()), source);
            final Parser parser = new Parser(lexer, new RichSymbolFactory());
            parser.options = options;

            return parse(parser);
        } catch (IOException ioException) {
            throw new ErrorMessage.InputError(ioException);
        }
    }

    /**
     * Run the {@link Parser}.
     * <p>
     * This method handles CUP and its approaches to error reporting.
     */
    @SuppressWarnings("unchecked")
    private static List<Bind> parse(Parser parser) throws ErrorMessage {
        try {
            Object result = parser.parse().value;
            return (List<Bind>) result;

        } catch (ErrorMessage errorMessage) {
            throw errorMessage;

        } catch (Exception cupException) {
            // Create a new CompileError for the Exception thrown by CUP.
            throw new ErrorMessage.InternalError(
                    "The CUP Parser encountered an exception: " + cupException,
                    cupException);
        }
    }
}
