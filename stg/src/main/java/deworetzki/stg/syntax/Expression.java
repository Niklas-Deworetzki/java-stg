package deworetzki.stg.syntax;

import deworetzki.parse.Position;

/**
 * An abstract superclass of all {@link Expression expressions}.
 */
public abstract class Expression extends Node {
    public Expression(Position position) {
        super(position);
    }
}
