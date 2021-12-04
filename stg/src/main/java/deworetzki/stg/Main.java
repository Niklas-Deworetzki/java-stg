package deworetzki.stg;

import deworetzik.stg.parse.Parser;
import deworetzki.parse.Source;
import deworetzki.parse.symbol.RichSymbolFactory;
import deworetzki.stg.parser.Scanner;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public final class Main {

    public static void main(String[] args) {
        for (String arg : args) {
            final File inputFile = new File(arg);
            if (inputFile.exists()) {
                final Source source = Source.fromFile(inputFile);
                try (Reader input = new InputStreamReader(new FileInputStream(inputFile))) {
                    final Scanner lexer = new Scanner(input, source);
                    final Parser parser = new Parser(lexer, new RichSymbolFactory());

                    parser.parse();

                    System.out.println(arg + ": Success!");
                } catch (Exception exception) {
                    System.out.println(exception.getMessage());
                }
            }
        }
    }
}
