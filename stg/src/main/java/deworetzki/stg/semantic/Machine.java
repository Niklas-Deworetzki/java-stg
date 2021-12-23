package deworetzki.stg.semantic;

import deworetzki.messages.ErrorMessage;
import deworetzki.stg.syntax.*;
import deworetzki.stg.visitor.DefaultVisitor;

import java.util.*;
import java.util.stream.Collectors;

import static deworetzki.stg.semantic.Value.*;
import static deworetzki.utils.CollectionUtils.combineWith;
import static deworetzki.utils.CollectionUtils.take;
import static java.util.Collections.emptyList;

public class Machine {
    public static final Expression ENTRY_POINT = new FunctionApplication(new Variable("main"), emptyList());

    private final Heap heap = new Heap();
    private final Map<Variable, Value> globalEnvironment;

    private static <A> Deque<A> emptyStack() {
        return new LinkedList<>();
    }

    // Initial State: Eval (main { })
    private Code code = new Code.Eval(ENTRY_POINT, Collections.emptyMap());

    private Deque<Value> argumentStack = emptyStack();
    private Deque<Continuation> returnStack = emptyStack();
    private final Deque<UpdateFrame> updateStack = emptyStack();

    public Machine(Program program) {
        this.globalEnvironment = allocateAll(heap, program.bindings, Collections.emptyMap(), true);
    }

    public void step() {
        if (code instanceof Code.Eval eval) {
            code = eval.expression().accept(new Evaluator(eval.locals()));

        } else if (code instanceof Code.Enter enter) {
            Closure closure = heap.get(enter.address()); // TODO: Blackhole?

            if (argumentStack.size() < closure.code().parameters.size()) {
                final List<Variable> updatedParameters = new ArrayList<>(closure.code().parameters.subList(argumentStack.size(), closure.code().parameters.size()));
                final List<Variable> updatedFreeVars = new ArrayList<>(closure.code().freeVariables);
                updatedFreeVars.addAll(closure.code().parameters.subList(0, argumentStack.size()));
                final List<Value> updatedBoundVals = new ArrayList<>(closure.capture());
                updatedBoundVals.addAll(argumentStack);

                final UpdateFrame frame = updateStack.pop();
                // Restore return stack and append restored argument stack.
                returnStack = frame.returnStack();
                argumentStack.addAll(frame.argumentStack());

                heap.update(frame.address(), new Closure(
                        new LambdaForm(updatedFreeVars, false, updatedParameters, heap.get(frame.address()).code().body),
                        updatedBoundVals
                ));

            } else {
                List<Value> arguments = take(closure.code().parameters.size(), argumentStack);

                final var localEnvironment = mkLocalEnv(
                        closure.code().freeVariables, closure.capture(),
                        closure.code().parameters, arguments);

                if (!closure.code().isUpdateable) {
                    code = new Code.Eval(closure.code().body, localEnvironment);
                } else {
                    updateStack.push(new UpdateFrame(argumentStack, returnStack, enter.address()));
                    argumentStack = emptyStack();
                    returnStack = emptyStack();

                    code = new Code.Eval(closure.code().body, localEnvironment);
                }
            }

        } else if (code instanceof Code.ReturnConstructor ret) {
            if (returnStack.isEmpty()) {
                final UpdateFrame frame = updateStack.pop();
                // Restore argument and return stack.
                argumentStack = frame.argumentStack();
                returnStack = frame.returnStack();

                // Update address with a new closure
                heap.update(frame.address(), standardConstructorClosure(ret));

            } else {
                final Continuation continuation = returnStack.pop();

                for (Alternative alternative : continuation.alternatives().alternatives) {
                    AlgebraicAlternative algebraicAlternative = (AlgebraicAlternative) alternative;
                    // Find matching alternative (if present) and exit early.
                    if (Constructor.areEqual(ret.constructor(), algebraicAlternative.constructor)) {
                        // FIXME: Add bound variables from alternative to environment
                        code = new Code.Eval(algebraicAlternative.expression, continuation.savedEnvironment());
                        return;
                    }
                }

                if (continuation.alternatives().defaultAlternative instanceof DefaultBindingAlternative def) {
                    // Build a closure that contains the returned constructor applied to its arguments.
                    Closure boundClosure = standardConstructorClosure(ret);

                    continuation.savedEnvironment().put(def.variable, new Address(heap.allocate(boundClosure)));
                    code = new Code.Eval(def.expression, continuation.savedEnvironment());

                } else if (continuation.alternatives().defaultAlternative instanceof DefaultFallthroughAlternative def) {
                    code = new Code.Eval(def.expression, continuation.savedEnvironment());
                } else {
                    throw new ErrorMessage.NoMatchingAlternative(continuation.alternatives(), ret);
                }
            }

        } else if (code instanceof Code.ReturnInteger ret) {
            final Continuation continuation = returnStack.pop();

            for (Alternative alternative : continuation.alternatives().alternatives) {
                PrimitiveAlternative primitiveAlternative = (PrimitiveAlternative) alternative;

                if (ret.integer() == primitiveAlternative.literal.value) {
                    code = new Code.Eval(primitiveAlternative.expression, continuation.savedEnvironment());
                    return;
                }
            }

            if (continuation.alternatives().defaultAlternative instanceof DefaultBindingAlternative def) {
                continuation.savedEnvironment().put(def.variable, new UnboxedInt(ret.integer()));
                code = new Code.Eval(def.expression, continuation.savedEnvironment());
            } else if (continuation.alternatives().defaultAlternative instanceof DefaultFallthroughAlternative def) {
                code = new Code.Eval(def.expression, continuation.savedEnvironment());
            } else {
                throw new ErrorMessage.NoMatchingAlternative(continuation.alternatives(), ret);
            }
        }
    }

    private final class Evaluator extends DefaultVisitor<Code> {
        private final Map<Variable, Value> localEnvironment;

        public Evaluator(Map<Variable, Value> localEnvironment) {
            super();
            this.localEnvironment = localEnvironment;
        }

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
            } else if (function instanceof UnboxedInt k) {
                return new Code.ReturnInteger(k.value());
            }

            return null;
        }

        @Override
        public Code visit(PrimitiveApplication application) {
            final int[] primitiveValues = values(localEnvironment, globalEnvironment, application.arguments).stream()
                    .mapToInt(Value::getValue)
                    .toArray();
            final int result = application.getOperation().applyAsInt(primitiveValues);
            return new Code.ReturnInteger(result);
        }

        @Override
        public Code visit(LetBinding let) {
            Map<Variable, Value> localEnvironment = allocateAll(heap, let.bindings, this.localEnvironment, let.isRecursive);
            return new Code.Eval(let.expression, localEnvironment);
        }

        @Override
        public Code visit(CaseExpression expression) {
            returnStack.push(new Continuation(expression.alternatives, localEnvironment));
            return new Code.Eval(expression.scrutinized, localEnvironment);
        }

        @Override
        public Code visit(ConstructorApplication application) {
            List<Value> arguments = values(localEnvironment, globalEnvironment, application.arguments);
            return new Code.ReturnConstructor(application.constructor, arguments);
        }

        @Override
        public Code visit(Literal literal) {
            return new Code.ReturnInteger(literal.value);
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

    private Map<Variable, Value> mkLocalEnv(final List<Variable> freeVariables,
                                            final List<Value> boundValues,
                                            final List<Variable> parameters,
                                            final List<Value> arguments) {
        Map<Variable, Value> localEnvironment = new HashMap<>(freeVariables.size() + parameters.size());
        combineWith(freeVariables, boundValues, localEnvironment::put);
        combineWith(parameters, arguments, localEnvironment::put);
        return localEnvironment;
    }

    private static Closure standardConstructorClosure(Code.ReturnConstructor ret) {
        final List<Variable> constructorArguments = Variable.arbitrary(ret.arguments().size());
        return new Closure(
                new LambdaForm(constructorArguments, false, emptyList(), new ConstructorApplication(ret.constructor(), constructorArguments)),
                ret.arguments()
        );
    }
}
