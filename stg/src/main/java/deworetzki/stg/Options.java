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

    @Unmatched
    List<String> unknownOptions = new ArrayList<>();


    public boolean shouldDisplayHelp() {
        return isHelpRequested || !unknownOptions.isEmpty();
    }

    @Override
    public Iterator<File> iterator() {
        return Arrays.asList(inputFiles).iterator();
    }


    enum DebugFlag {
        LEXER, NONE
    }

    enum Extensions {
        ALLOW_NONPRIMITIVE_NUMBERS,
        ALLOW_DOUBLE_CASE_ARROW,
        ELIDE_EMPTY_ATOMS,
    }

}
