package deworetzki.stg.semantic;

import deworetzki.stg.syntax.Atom;
import deworetzki.stg.syntax.Constructor;
import deworetzki.stg.syntax.Expression;
import deworetzki.stg.syntax.Variable;

import java.util.List;
import java.util.Map;

/**
 * The <i>code</i> component of the {@link Machine machine state} can take one of the
 * four forms defined in this file.
 */
public sealed interface Code {

    /**
     * Evaluate the given {@link Expression} in the given {@link Map Environment}
     * and apply its value to the arguments on the argument stack.
     * <p>
     * The {@link Expression} is an arbitrarily complex STG-language expression.
     */
    record Eval(Expression expression, Map<Variable, Value> locals) implements Code {
    }

    /**
     * Apply the {@link Closure} at the given address to the arguments on the
     * argument stack.
     */
    record Enter(int address) implements Code {
    }

    /**
     * Return the given {@link Constructor} applied to the given {@link Value values}
     *  to the continuation on the return stack.
     */
    record ReturnConstructor(Constructor constructor, List<Value> arguments) implements Code {
    }

    /**
     * Return the primitive integer to the continuation on the return stack.
     */
    record ReturnInteger(int integer) implements Code {
    }
}
