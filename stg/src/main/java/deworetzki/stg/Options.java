package deworetzki.stg;

import java.io.File;
import java.util.*;

import static picocli.CommandLine.*;

@Command(

)
public final class Options implements Iterable<File> {
    @Parameters(paramLabel = "INPUT")
    File[] inputFiles = {null};

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
    public Iterator<File> iterator() {
        return Arrays.asList(inputFiles).iterator();
    }


    public enum DebugFlag {
        LEXER, NONE
    }

    public enum Extensions {
        ALLOW_NONPRIMITIVE_NUMBERS;

        public String getHint() {
            return "Did you mean to enable the '" + this + "' extension?";
        }
    }

}
