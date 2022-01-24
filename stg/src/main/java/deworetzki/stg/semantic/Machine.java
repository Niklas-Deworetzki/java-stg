package deworetzki.stg.semantic;

import deworetzki.messages.ErrorMessage;
import deworetzki.stg.syntax.*;
import deworetzki.stg.visitor.DefaultVisitor;

import java.util.*;
import java.util.stream.Collectors;

import static deworetzki.stg.semantic.Value.*;
import static deworetzki.utils.CollectionUtils.combineWith;
import static deworetzki.utils.CollectionUtils.take;
import static java.util.Collections.*;

public class Machine {
    public static final Expression ENTRY_POINT = new FunctionApplication(new Variable("main"), emptyList());

    private final Heap heap = new Heap();
    private final Map<Variable, Value> globalEnvironment;
    private final Evaluator evaluator = new Evaluator();

    private static <A> Deque<A> emptyStack() {
        return new LinkedList<>();
    }

    // Initial State: Eval (main { })
    private State state = new Eval(ENTRY_POINT, emptyMap());

    private Deque<Value> argumentStack = emptyStack();
    private Deque<Continuation> returnStack = emptyStack();
    private final Deque<UpdateFrame> updateStack = emptyStack();

    public Machine(Program program) {
        this.globalEnvironment = allocateAll(heap, program.bindings, emptyMap(), true);
    }

    public void step() {
        state = state.transfer(this);
    }


    /**
     * The {@link Machine machine state} can take on different forms, that describe
     * how the next state can be reached.
     */
    public sealed interface State {
        State transfer(final Machine machine);
    }

    /**
     * Evaluate the given {@link Expression} in the given {@link Map Environment}
     * and apply its value to the arguments on the argument stack.
     * <p>
     * The {@link Expression} is an arbitrarily complex STG-language expression.
     */
    public record Eval(Expression expression, Map<Variable, Value> locals) implements State {
        @Override
        public State transfer(final Machine machine) {
            return machine.evaluator.evalUnderEnvironment(expression, locals);
        }
    }

    /**
     * Apply the {@link Closure} at the given address to the arguments on the
     * argument stack.
     */
    public record Enter(int address) implements State {
        @Override
        public State transfer(final Machine machine) {
            Closure closure = machine.heap.get(address); // TODO: Blackhole?

            if (machine.argumentStack.size() < closure.code().parameters.size()) {
                // Not enough arguments to enter the closure. We encountered an update.
                final List<Variable> updatedParameters = new ArrayList<>(closure.code().parameters.subList(machine.argumentStack.size(), closure.code().parameters.size()));
                final List<Variable> updatedFreeVars = new ArrayList<>(closure.code().freeVariables);
                updatedFreeVars.addAll(closure.code().parameters.subList(0, machine.argumentStack.size()));
                final List<Value> updatedBoundVals = new ArrayList<>(closure.capture());
                updatedBoundVals.addAll(machine.argumentStack);

                final UpdateFrame frame = machine.updateStack.pop();
                frame.update(machine, updated -> new Closure( // TODO: Do we need the previous closure here?
                        new LambdaForm(updatedFreeVars, false, updatedParameters, updated.code().body),
                        updatedBoundVals
                ));

                return this; // Try again!

            } else {
                List<Value> arguments = take(closure.code().parameters.size(), machine.argumentStack);

                final var localEnvironment = mkLocalEnv(
                        closure.code().freeVariables, closure.capture(),
                        closure.code().parameters, arguments);

                if (closure.code().isUpdateable) {
                    machine.updateStack.push(new UpdateFrame(machine.argumentStack, machine.returnStack, address));
                    machine.argumentStack = emptyStack();
                    machine.returnStack = emptyStack();
                }
                return new Eval(closure.code().body, localEnvironment);
            }
        }
    }

    /**
     * Return the given {@link Constructor} applied to the given {@link Value values}
     * to the continuation on the return stack.
     */
    public record ReturnConstructor(Constructor constructor, List<Value> arguments) implements State {
        @Override
        public State transfer(Machine machine) {
            if (machine.returnStack.isEmpty()) {
                final UpdateFrame frame = machine.updateStack.pop();
                frame.update(machine, ignored -> standardConstructorClosure());
                return this; // Try again.

            } else {
                final Continuation continuation = machine.returnStack.pop();

                for (Alternative alternative : continuation.alternatives().alternatives) {
                    AlgebraicAlternative algebraicAlternative = (AlgebraicAlternative) alternative;
                    // Find matching alternative (if present) and exit early.
                    if (Constructor.areEqual(constructor, algebraicAlternative.constructor)) {
                        combineWith(algebraicAlternative.arguments.iterator(), arguments.iterator(),
                                continuation.savedEnvironment()::put);
                        return new Eval(algebraicAlternative.expression, continuation.savedEnvironment());
                    }
                }

                if (continuation.alternatives().defaultAlternative instanceof DefaultBindingAlternative def) {
                    // Build a closure that contains the returned constructor applied to its arguments.
                    Closure boundClosure = standardConstructorClosure();

                    continuation.savedEnvironment().put(def.variable, new Address(machine.heap.allocate(boundClosure)));
                    return new Eval(def.expression, continuation.savedEnvironment());

                } else if (continuation.alternatives().defaultAlternative instanceof DefaultFallthroughAlternative def) {
                    return new Eval(def.expression, continuation.savedEnvironment());
                } else {
                    throw new ErrorMessage.NoMatchingAlternative(continuation.alternatives(), this);
                }
            }
        }

        private Closure standardConstructorClosure() {
            final List<Variable> constructorArguments = Variable.arbitrary(arguments().size());
            return new Closure(
                    new LambdaForm(constructorArguments, false, emptyList(), new ConstructorApplication(constructor(), constructorArguments)),
                    arguments()
            );
        }
    }

    /**
     * Return the primitive integer to the continuation on the return stack.
     */
    public record ReturnInteger(int integer) implements State {
        @Override
        public State transfer(Machine machine) {
            if (machine.returnStack.isEmpty()) {
                final UpdateFrame frame = machine.updateStack.pop();
                frame.update(machine, ignored -> standardIntegerClosure());
                return this; // Try again.

            } else {
                final Continuation continuation = machine.returnStack.pop();

                for (Alternative alternative : continuation.alternatives().alternatives) {
                    PrimitiveAlternative primitiveAlternative = (PrimitiveAlternative) alternative;

                    if (integer == primitiveAlternative.literal.value) {
                        return new Eval(primitiveAlternative.expression, continuation.savedEnvironment());
                    }
                }

                if (continuation.alternatives().defaultAlternative instanceof DefaultBindingAlternative def) {
                    continuation.savedEnvironment().put(def.variable, new Int(integer));
                    return new Eval(def.expression, continuation.savedEnvironment());
                } else if (continuation.alternatives().defaultAlternative instanceof DefaultFallthroughAlternative def) {
                    return new Eval(def.expression, continuation.savedEnvironment());
                } else {
                    throw new ErrorMessage.NoMatchingAlternative(continuation.alternatives(), this);
                }
            }
        }

        private Closure standardIntegerClosure() {
            return new Closure(
                    new LambdaForm(emptyList(), false, emptyList(),
                            new Literal(integer)),
                    emptyList());
        }
    }

    private final class Evaluator extends DefaultVisitor<State> {
        private Map<Variable, Value> localEnvironment;

        public Evaluator() {
            super();
        }

        public synchronized State evalUnderEnvironment(Expression expression, Map<Variable, Value> environment) {
            this.localEnvironment = environment;
            return expression.accept(this);
        }

        @Override
        public State visit(FunctionApplication application) {
            final var function = value(localEnvironment, globalEnvironment, application.function);
            if (function instanceof Address a) {
                // Evaluate arguments
                final List<Value> arguments = values(localEnvironment, globalEnvironment, application.arguments);

                // And push them onto the argument stack.
                for (int i = arguments.size() - 1; i >= 0; i--) {
                    argumentStack.push(arguments.get(i));
                }

                // Enter the closure of the function.
                return new Enter(a.address());
            } else if (function instanceof Int k) {
                return new ReturnInteger(k.value());
            }

            throw new ErrorMessage.InternalError(application.function.position, application.function + " is " + function);
        }

        @Override
        public State visit(PrimitiveApplication application) {
            final int[] primitiveValues = values(localEnvironment, globalEnvironment, application.arguments).stream()
                    .mapToInt(Value::getValue)
                    .toArray();
            final int result = application.getOperation().applyAsInt(primitiveValues);
            return new ReturnInteger(result);
        }

        @Override
        public State visit(LetBinding let) {
            Map<Variable, Value> localEnvironment = allocateAll(heap, let.bindings, this.localEnvironment, let.isRecursive);
            return new Eval(let.expression, localEnvironment);
        }

        @Override
        public State visit(CaseExpression expression) {
            returnStack.push(new Continuation(expression.alternatives, localEnvironment));
            return new Eval(expression.scrutinized, localEnvironment);
        }

        @Override
        public State visit(ConstructorApplication application) {
            List<Value> arguments = values(localEnvironment, globalEnvironment, application.arguments);
            return new ReturnConstructor(application.constructor, arguments);
        }

        @Override
        public State visit(Literal literal) {
            return new ReturnInteger(literal.value);
        }

    }

    /**
     * Allocate a {@link List} of {@link Bind bindings} on the {@link Heap}
     * and in a {@link Map local environment}, that is returned from this method.
     * <p>
     * For every binding provided, a {@link Address heap address} is reserved,
     * which is entered as the {@link Value} for the bindings {@link Variable}
     * into the given {@link Map environment}.
     * <p>
     * After the environment has been prepared, the heap space at the reserved address
     * for a binding is {@link Heap#update(int, Closure) updated} with a newly created
     * {@link Closure}. This will capture the value visible value for every
     * {@link LambdaForm#freeVariables free variable} in the closures {@link LambdaForm}.
     * <p>
     * Capturing the closures free variables can also happen recursively, fetching the
     * values from the local built environment instead of the outer one.
     *
     * @param heap             The {@link Heap} object used to allocate a {@link Closure}
     *                         for every given {@link LambdaForm}.
     * @param bindings         The {@link Bind bindings} inserted into the returned
     *                         {@link Map environment}. The {@link LambdaForm} of these
     *                         bindings is allocated on the {@link Heap}.
     * @param outerEnvironment The outer {@link Map environment} under which the bindings
     *                         are present.
     * @param isRecursive      If true, the {@link LambdaForm#freeVariables free variables}
     *                         of the {@link LambdaForm} may be bound to the variables
     *                         generated from the {@link Bind bindings}.
     * @return An updated {@link Map environment}, holding the {@link Address heap address}
     * for every {@link Bind}.
     */
    private static Map<Variable, Value> allocateAll(final Heap heap, final List<Bind> bindings,
                                                    final Map<Variable, Value> outerEnvironment,
                                                    final boolean isRecursive) {
        // Create a local environment to mutate, including all bindings from the outer one.
        final Map<Variable, Value> localEnvironment = new HashMap<>(outerEnvironment);
        // Reserve an address for all values on the heap.
        final Iterable<Integer> addresses = heap.reserveMany(bindings.size());
        // Update the local environment using the reserved heap addresses.
        combineWith(addresses, bindings,
                (address, bind) -> localEnvironment.put(bind.variable, new Address(address)));

        // Recursive definitions may refer to themselves.
        final Map<Variable, Value> rhsEnvironment = (isRecursive) ? localEnvironment : outerEnvironment;
        // Update the Closures for the reserved addresses.
        combineWith(addresses, bindings, (address, bind) -> {
            List<Value> capturedValues = bind.lambda.freeVariables.stream()
                    .map(rhsEnvironment::get)
                    .collect(Collectors.toList());
            heap.update(address, new Closure(bind.lambda, capturedValues));
        });
        return localEnvironment;
    }

    private static Map<Variable, Value> mkLocalEnv(final List<Variable> freeVariables,
                                                   final List<Value> boundValues,
                                                   final List<Variable> parameters,
                                                   final List<Value> arguments) {
        Map<Variable, Value> localEnvironment = new HashMap<>(freeVariables.size() + parameters.size());
        combineWith(freeVariables, boundValues, localEnvironment::put);
        combineWith(parameters, arguments, localEnvironment::put);
        return localEnvironment;
    }

    public State getState() {
        return state;
    }

    public Heap getHeap() {
        return heap;
    }

    public Map<Variable, Value> getGlobalEnvironment() {
        return globalEnvironment;
    }

    public Deque<Value> getArgumentStack() {
        return argumentStack;
    }

    public Deque<Continuation> getReturnStack() {
        return returnStack;
    }

    public Deque<UpdateFrame> getUpdateStack() {
        return updateStack;
    }
}
