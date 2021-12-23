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

public final class Main {
    public static void main(String[] args) {
        final Options options = new Options();
        try {
            CommandLine.populateCommand(options, args);
        } catch (CommandLine.PicocliException ignored) {
            options.isHelpRequested = true;
        }

        if (options.shouldDisplayHelp()) {
            CommandLine.usage(options, System.out);
        } else {
            try {
                loadProgram(options).ifPresent(new StgRuntime(options)::run);
            } catch (ErrorMessage errorMessage) {
                System.out.println(errorMessage.toAnsi());
            }
        }
    }

    private static Optional<Program> loadProgram(final Options options) {
        final List<Bind> inputProgram = new ArrayList<>();

        if (options.loadPrelude()) {
            try {
                inputProgram.addAll(loadPrelude(options));
            } catch (Exception failedToLoadPrelude) {
                System.out.println(new ErrorMessage.InternalError("Failed to load prelude: " +
                        failedToLoadPrelude.getMessage()).toAnsi());
                return Optional.empty();
            }
        }

        boolean anyFailed = false;
        for (var inputFile : options) {
            try {
                inputProgram.addAll(loadInput(options, inputFile));
            } catch (ErrorMessage errorMessage) {
                anyFailed = true;
                System.out.println(errorMessage.toAnsi());
            }
        }

        if (anyFailed) return Optional.empty();
        return Optional.of(new Program(inputProgram));
    }

    private static List<Bind> loadPrelude(final Options options) throws ErrorMessage {
        return Collections.emptyList(); // TODO: Load from internal resource.
    }

    private static List<Bind> loadInput(final Options options, final ResourceProvider<Source, IOException> resource) throws ErrorMessage {
        try (Source source = resource.get()) {
            final Scanner lexer = new Scanner(new InputStreamReader(source.getInputStream()), source);
            final Parser parser = new Parser(lexer, new RichSymbolFactory());
            parser.options = options;

            final List<Bind> bindings = parse(parser);
            return Objects.requireNonNullElse(bindings, Collections.emptyList());
        } catch (IOException ioException) {
            throw new ErrorMessage.InputError(ioException);
        }
    }

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
