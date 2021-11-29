package deworetzki.parse;

import deworetzki.parse.symbol.RichSymbol;
import java_cup.runtime.Symbol;

public interface PositionedLexer {
    Position currentPosition();

    default Symbol symbol(int type) {
        return new RichSymbol(currentPosition(), type);
    }

    default Symbol symbol(int type, Object value) {
        return new RichSymbol(currentPosition(), type, value);
    }
}
