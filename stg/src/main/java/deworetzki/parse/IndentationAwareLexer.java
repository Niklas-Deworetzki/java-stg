package deworetzki.parse;

import deworetzik.stg.parse.Sym;
import deworetzki.stg.parser.Scanner;
import java_cup.runtime.Symbol;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

public class IndentationAwareLexer extends Scanner {
    private final Queue<Symbol> queuedTokens = new ArrayDeque<>();
    private final Deque<Integer> indentationStack = new ArrayDeque<>();

    { // Always include a 0 on the indentation stack.
        indentationStack.add(0);
    }

    public IndentationAwareLexer(Reader in, Source source) {
        super(in, source);
    }

    @Override
    public Symbol next_token() throws IOException {
        if (!queuedTokens.isEmpty()) {
            return queuedTokens.poll();
        }

        return super.next_token();
    }

    private int indentationLevel(String indentation) {
        // TODO: Warn if spaces and tabs are mixed.
        return indentation.length();
    }

    @Override
    protected void handleIndentation(String indentation) {
        final int targetLevel = indentationLevel(indentation);
        final int currentLevel = indentationStack.getFirst();

        if (targetLevel > currentLevel) {
            indentationStack.push(targetLevel);
            queuedTokens.add(symbol(Sym.INDENT));
        } else if (targetLevel < currentLevel) {
            while (indentationStack.getFirst() < currentLevel) {
                indentationStack.pop();
                queuedTokens.add(symbol(Sym.UNINDENT));
            }
        }
    }
}
