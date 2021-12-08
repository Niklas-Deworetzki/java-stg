package deworetzki.stg;

import deworetzki.parse.Source;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.Set;

public final class Options {
    public DebugFlag debugFlag;
    public Set<Extensions> extensions = EnumSet.noneOf(Extensions.class);
    private File inputFile;

    public Options(String[] args) {
        for (String arg : args) {
            enableOption(arg);
        }
    }

    public Source openSource() throws FileNotFoundException {
        return Source.fromFile(inputFile);
    }

    private void enableOption(String arg) {
        switch (arg) {
            case "--debug-lexer" -> debugFlag = DebugFlag.LEXER;
            case "--allow-nonprimitive-numbers" -> extensions.add(Extensions.ALLOW_NONPRIMITIVE_NUMBERS);
            case "--allow-double-case-arrow" -> extensions.add(Extensions.ALLOW_DOUBLE_CASE_ARROW);
            default -> inputFile = new File(arg);
        }
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
