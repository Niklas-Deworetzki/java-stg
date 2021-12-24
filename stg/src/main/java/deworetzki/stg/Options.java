package deworetzki.stg;

import deworetzki.parse.Source;
import deworetzki.utils.ResourceProvider;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static picocli.CommandLine.*;

/**
 * This data class implements a command line interface via the <i>picocli</i> library.
 */
@Command(
        name = "stg",
        description = "This program implements the STG language and machine as described by " +
                "Simon Peyton Jones in the 1992 paper: \"Implementing lazy functional languages " +
                "on stock hardware - the Spineless Tagless G-machine\"",
        footer = "(C) 2021, Niklas Deworetzki"
)
public final class Options implements Iterable<ResourceProvider<Source, IOException>> {
    @Parameters(paramLabel = "INPUT",
            description = "One or more input files to parse. Definitions from all input files are combined " +
                    "into a single program with a single entry point.")
    File[] inputFiles;

    @Option(names = {"--enable", "-e"},
            description = "Enable extensions, which weaken the restrictions of the STG language but " +
                    "make programming easier. Supported extensions are: ${COMPLETION-CANDIDATES}")
    Set<Extensions> extensions = EnumSet.noneOf(Extensions.class);

    @Option(names = {"--help", "-h"}, usageHelp = true,
            description = "Show a help page describing all supported flags.")
    boolean isHelpRequested;

    @Option(names = {"--no-prelude", "-n"},
            description = "Do not load the prelude. No additional definitions will be available.")
    boolean noPrelude;

    @Unmatched
    List<String> unknownOptions = new ArrayList<>();

    /**
     * Returns <code>true</code> if the prelude should be loaded.
     */
    public boolean loadPrelude() {
        return !noPrelude;
    }

    /**
     * Returns <code>true</code> if a help message should be displayed.
     * <p>
     * This is the case, if either the user specifies <code>--help</code> directly,
     * or if an unknown option is specified.
     */
    public boolean shouldDisplayHelp() {
        return isHelpRequested || !unknownOptions.isEmpty();
    }

    /**
     * Checks whether the given {@link Extensions Extension} is enabled by the user.
     */
    public boolean isExtensionEnabled(Extensions extension) {
        return extensions.contains(extension);
    }

    /**
     * Returns an {@link Iterator} over all input {@link Source Sources}.
     */
    @Override
    public Iterator<ResourceProvider<Source, IOException>> iterator() {
        if (inputFiles == null || inputFiles.length == 0) {
            return List.of((ResourceProvider<Source, IOException>) Source::fromStdIn).iterator();
        }
        return Arrays.stream(inputFiles).map(file -> (ResourceProvider<Source, IOException>) () -> Source.fromFile(file))
                .iterator();
    }

    /**
     * An enum of supported {@link Extensions}, which aim to weaken the restrictions
     * of the STG language to make programming easier.
     */
    public enum Extensions {
        /**
         * Allows the use of <code>4</code> instead of <code>4#</code>. This will apply the
         * <code>Int</code> constructor to the given primitive value, making the resulting
         * object an Expression rather than an Atom, which might be bound to a Variable.
         */
        ALLOW_NONPRIMITIVE_NUMBERS("nonprimitive-numbers"),
        /**
         * Tries to analyze used constructors to enforce a uniform amount of
         * parameters for every occurrence of a constructor.
         * <p>
         * During Analysis the program has to guess, how many parameters a
         * constructor should have. Consequently, errors might be reported
         * at the wrong position.
         */
        ANALYZE_CONSTRUCTOR_ARGS("check-constructors"),
        /**
         * Allows omitting the free variable list in a lambda form, telling
         * the program to infer free variables instead.
         */
        INFER_FREE_VARIABLES("infer-free"),
        /**
         * Allows the use of expressions instead of a lambda form. The parser
         * will insert a lambda without parameters holding the expression
         * in its body.
         * <p>
         * This requires inferring the free variables.
         */
        EXPRESSION_AS_LAMBDA("expression-as-lambda");

        private final String representation;

        Extensions(String representation) {
            this.representation = representation;
        }

        /**
         * Constructs a message, asking the user to enable this {@link Extensions}.
         */
        public String getHint() {
            return "Did you mean to enable the '" + this + "' extension? (--enable " + this + ")";
        }


        @Override
        public String toString() {
            return representation;
        }
    }
}
