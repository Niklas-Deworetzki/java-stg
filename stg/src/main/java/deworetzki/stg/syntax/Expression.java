package deworetzki.stg.syntax;

import deworetzki.parse.Position;

/**
 * An abstract superclass of all {@link Expression expressions}.
 */
public abstract sealed class Expression extends Node permits Application, CaseExpression, LetBinding, Literal {
    public Expression(Position position) {
        super(position);
    }
}
