package deworetzki.stg;

import deworetzik.stg.parse.Parser;
import deworetzki.messages.ErrorMessage;
import deworetzki.parse.Source;
import deworetzki.parse.symbol.RichSymbolFactory;
import deworetzki.stg.parser.Scanner;
import deworetzki.stg.syntax.Bind;
import deworetzki.stg.syntax.Program;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Main {
    public static void main(String[] args) {
        final Options options = CommandLine.populateCommand(new Options(), args);

        if (options.shouldDisplayHelp()) {
            CommandLine.usage(options, System.out);
        } else {

            final List<Bind> inputProgram = new ArrayList<>();

            for (File inputFile : options) {
                try {
                    inputProgram.addAll(loadInputFile(options, inputFile));
                } catch (ErrorMessage errorMessage) {
                    System.out.println(errorMessage.toAnsi());
                }
            }

            System.out.println("Loaded " + inputProgram.size() + " Bindings.");
        }
    }

    private static List<Bind> loadInputFile(final Options options, final File file) throws ErrorMessage {
        try (Source source = Source.fromFile(file)) {
            final Scanner lexer = new Scanner(new InputStreamReader(source.getInputStream()), source);
            final Parser parser = new Parser(lexer, new RichSymbolFactory());

            final Program program = parse(parser);
            if (program != null) {
                return program.bindings;
            }
            return Collections.emptyList();
        } catch (IOException ioException) {
            throw new ErrorMessage.InputError(ioException);
        }
    }

    private static Program parse(Parser parser) throws ErrorMessage {
        try {
            Object result = parser.parse().value;
            return (Program) result;

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
