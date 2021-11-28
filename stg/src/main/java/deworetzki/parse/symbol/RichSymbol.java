package deworetzki.parse.symbol;

import deworetzki.parse.Position;
import java_cup.runtime.Symbol;

import deworetzik.stg.parse.Sym;

/**
 * A custom Symbol class used by Scanner and Parser.
 * This class holds a {@link Position} value, representing the original position in the source code.
 * Additionally this class uses the generated {@link Sym} class, to lookup names for terminal symbols.
 */
public class RichSymbol extends Symbol {
    public final Position position;

    /**
     * Is this a terminal symbol? This decision is made depending on the invoked constructor.
     */
    private final boolean isTerminal;
    private final String name;

    public RichSymbol(Position position, int terminalId, Object value) {
        super(terminalId, position.line(), position.column(), value);
        this.position = position;
        this.isTerminal = true;
        this.name = Sym.terminalNames[terminalId];
    }

    public RichSymbol(Position position, int terminalId) {
        this(position, terminalId, null);
    }

    public RichSymbol(Position position, int nonterminalId, String nonterminalName, int left, int right, Object value) {
        super(nonterminalId, left, right, value);
        this.position = position;
        this.isTerminal = false;
        this.name = nonterminalName;
    }

    public RichSymbol(Position position, int nonterminalId, String nonterminalName, int left, int right) {
        this(position, nonterminalId, nonterminalName, left, right, null);
    }

    /**
     * The name of this Symbol. It is determined depending on whether this symbol represents a
     * terminal symbol or a nonterminal symbol.
     * <p>
     * For terminal symbols the symbol's name is obtained by looking it up in {@link Sym#terminalNames} with the
     * symbol's terminal id. For nonterminal symbols the name must be provided when invoking the constructor.
     * </p>
     *
     * @return This symbol's name.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (isTerminal && value != null) {
            return String.format("%s (%s)", name, value);
        } else {
            return name;
        }
    }
}
