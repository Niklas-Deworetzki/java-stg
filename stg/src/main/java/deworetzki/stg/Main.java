package deworetzki.stg;

import deworetzik.stg.parse.Parser;
import deworetzik.stg.parse.Sym;
import deworetzki.parse.Source;
import deworetzki.parse.symbol.RichSymbolFactory;
import deworetzki.stg.parser.Scanner;
import java_cup.runtime.Symbol;

import java.io.InputStreamReader;

public final class Main {

    public static void main(String[] args) {
        final Options options = new Options(args);

        try (Source source = options.openSource()) {
            final Scanner lexer = new Scanner(new InputStreamReader(source.getInputStream()), source);
            if (options.debugFlag == Options.DebugFlag.LEXER) {
                Symbol symbol;
                do {
                    symbol = lexer.next_token();
                    dumpToken(symbol);
                } while (symbol.sym != Sym.EOF);
                return;
            }

            final Parser parser = new Parser(lexer, new RichSymbolFactory());

            parser.parse();

            System.out.println(source.getName() + ": Success!");
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void dumpToken(Symbol symbol) {
        String tokenName = Sym.terminalNames[symbol.sym];
        if (symbol.value == null) System.out.println(tokenName);
        else System.out.printf("%s (%s)%n", tokenName, symbol.value);
    }
}
