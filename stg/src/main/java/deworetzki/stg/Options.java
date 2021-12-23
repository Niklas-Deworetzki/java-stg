package deworetzki.stg;

import deworetzki.parse.Source;
import deworetzki.utils.ResourceProvider;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static picocli.CommandLine.*;

@Command(

)
public final class Options implements Iterable<ResourceProvider<Source, IOException>> {
    @Parameters(paramLabel = "INPUT")
    File[] inputFiles;

    @Option(names = {"--debug", "-d"}, description = "${COMPLETION-CANDIDATES}")
    DebugFlag debugFlag = DebugFlag.NONE;

    @Option(names = {"--enable", "-e"}, description = "${COMPLETION-CANDIDATES}")
    Set<Extensions> extensions = EnumSet.noneOf(Extensions.class);

    @Option(names = {"--help", "-h"}, usageHelp = true)
    boolean isHelpRequested;

    @Option(names = {"--no-prelude", "-n"})
    boolean noPrelude;

    @Unmatched
    List<String> unknownOptions = new ArrayList<>();

    public boolean loadPrelude() {
        return !noPrelude;
    }

    public boolean shouldDisplayHelp() {
        return isHelpRequested || !unknownOptions.isEmpty();
    }

    public boolean isExtensionEnabled(Extensions extension) {
        return extensions.contains(extension);
    }

    @Override
    public Iterator<ResourceProvider<Source, IOException>> iterator() {
        if (inputFiles == null || inputFiles.length == 0) {
            return List.of((ResourceProvider<Source, IOException>) Source::fromStdIn).iterator();
        }
        return Arrays.stream(inputFiles).map(file -> (ResourceProvider<Source, IOException>) () -> Source.fromFile(file))
                .iterator();
    }


    public enum DebugFlag {
        LEXER, NONE
    }

    public enum Extensions {
        ALLOW_NONPRIMITIVE_NUMBERS,
        ANALYZE_CONSTRUCTOR_ARGS,
        EXPRESSION_AS_LAMBDA,
        INFER_FREE_VARIABLES;

        public String getHint() {
            return "Did you mean to enable the '" + this + "' extension?";
        }
    }

}
