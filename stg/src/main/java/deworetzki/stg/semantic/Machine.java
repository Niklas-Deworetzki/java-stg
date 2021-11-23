package deworetzki.stg.semantic;

import deworetzki.parse.Position;
import deworetzki.stg.syntax.*;
import deworetzki.stg.visitor.DefaultVisitor;

import java.util.*;

import static deworetzki.stg.semantic.Value.*;
import static deworetzki.stg.utils.CollectionUtils.*;

public class Machine {
    public static final Expression ENTRY_POINT = new Variable(Position.NONE, "main");

    private Code code = new Code.Eval(ENTRY_POINT, Collections.emptyMap());
    private final Deque<Value> argumentStack = new LinkedList<>();
    private final Deque<Continuation> returnStack = new LinkedList<>();
    private final Deque<UpdateFrame> updateStack = new LinkedList<>();
    private final Heap heap = new Heap();
    private final Map<Variable, Value> globalEnvironment;


    public Machine(Program program) {
        this.globalEnvironment = heap.initialize(program);
    }

    public void step() {
        if (code instanceof Code.Eval eval) {
            code = eval.expression().accept(new Evaluator(eval.locals()));

        } else if (code instanceof Code.Enter enter) {
            Closure closure = heap.get(enter.address()); // TODO: Blackhole?
            // TODO: length(as) >= length(xs) must be true?
            // TODO: closure.code().isUpdateable == false

            List<Value> arguments = take(closure.code().arguments.size(), argumentStack);

            final var localEnvironment = mkLocalEnv(
                    closure.code().freeVariables, closure.values(),
                    closure.code().arguments, arguments);
            code = new Code.Eval(closure.code().body, localEnvironment);
        }
    }

    private final class Evaluator extends DefaultVisitor<Code> {
        private final Map<Variable, Value> localEnvironment;

        public Evaluator(Map<Variable, Value> localEnvironment) {
            super();
            this.localEnvironment = localEnvironment;
        }

        /**
         * <pre>
         * Eval (f xs) p as rs us h s
         *  with
         *      val p s f = Addr a
         * </pre>
         * =>
         * <pre>
         * Enter a ((val p s xs) ++ as) rs us h s
         * </pre>
         */
        @Override
        public Code visit(FunctionApplication application) {
            final var function = value(localEnvironment, globalEnvironment, application.function);
            if (function instanceof Address a) {
                List<Value> arguments = new ArrayList<>(application.arguments.size());

                // Evaluate arguments
                for (Atom argument : application.arguments) {
                    arguments.add(value(localEnvironment, globalEnvironment, argument));
                }

                // And push them onto the argument stack.
                for (int i = arguments.size() - 1; i >= 0; i--) {
                    argumentStack.push(arguments.get(i));
                }

                // Enter the closure of the function.
                return new Code.Enter(a.address());
            }

            return null;
        }
    }

    private Map<Variable, Value> mkLocalEnv(final List<Variable> freeVariables,
                                            final List<Value> boundValues,
                                            final List<Variable> parameters,
                                            final List<Value> arguments) {
        Map<Variable, Value> localEnvironment = new HashMap<>(freeVariables.size() + parameters.size());
        combineWith(freeVariables, boundValues, localEnvironment::put);
        combineWith(parameters, arguments, localEnvironment::put);
        return localEnvironment;
    }
}
