package deworetzki.stg.analysis;

import deworetzki.messages.ErrorMessage;
import deworetzki.messages.WarningMessage;
import deworetzki.stg.PrimitiveOperations;
import deworetzki.stg.Options;
import deworetzki.stg.syntax.*;
import deworetzki.stg.visitor.Visitor;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static deworetzki.utils.CollectionUtils.union;
import static deworetzki.utils.CollectionUtils.without;

public final class Analysis implements Visitor<Set<Variable>> {
    public static Analysis runOn(Program program, Options options) {
        final Analysis analysis = new Analysis(options);
        program.accept(analysis);
        return analysis;
    }

    private final Options options;

    public Analysis(Options options) {
        this.options = options;
    }

    private long reportedError = 0;

    private void report(final ErrorMessage errorMessage) {
        reportedError += 1;
        errorMessage.report();
    }

    public boolean hasReportedErrors() {
        return reportedError != 0;
    }

    private final Deque<Set<Variable>> scopes = new ArrayDeque<>();
    private final Set<Variable> reportedVariables = new HashSet<>();

    private Set<Variable> currentScope() {
        return scopes.peekFirst();
    }

    private <V> V withScope(Collection<Variable> variables, Supplier<V> action) {
        final Set<Variable> scope = new HashSet<>();

        for (Variable variable : variables) {
            if (!scope.add(variable)) {
                report(new ErrorMessage.Redeclaration(variable));
            }
        }

        scopes.push(scope);
        final V result = action.get();
        scopes.pop();

        return result;
    }

    private Stream<Variable> visibleVariables() {
        return scopes.stream().flatMap(Set::stream);
    }

    private boolean isDefined(Variable variable) {
        return scopes.stream().anyMatch(set -> set.contains(variable));
    }

    private boolean isFirstReport(Variable variable) {
        return reportedVariables.add(variable);
    }

    private final Map<Constructor, Integer> detectedArgumentCounts = new HashMap<>();

    private void verifyConstructor(Constructor constructor, int argumentCount) {
        if (!options.isExtensionEnabled(Options.Extensions.ANALYZE_CONSTRUCTOR_ARGS)) return;

        Integer detectedCount = detectedArgumentCounts.put(constructor, argumentCount);
        if (detectedCount != null && detectedCount != argumentCount) {
            new WarningMessage.ConstructorArgsDiffer(constructor, argumentCount, detectedCount).report();
        }
    }

    private final Set<Variable> globalVariables = new HashSet<>();

    private void checkMain(LambdaForm mainLambda) {
        if (!mainLambda.parameters.isEmpty()) {
            report(new ErrorMessage.MainWithParameters(mainLambda));
        }
    }

    @Override
    public Set<Variable> visit(Program program) {
        final List<Variable> listOfGlobalVariables = program.bindings.stream()
                .map(bind -> bind.variable).collect(Collectors.toList());
        withScope(listOfGlobalVariables, () -> globalVariables.addAll(currentScope()));

        boolean mainFound = false;
        for (Bind bind : program) {
            if (bind.variable.name.equals("main")) {
                mainFound = true;
                checkMain(bind.lambda);
            }

            withScope(globalVariables, () -> bind.accept(this));
            reportedVariables.clear(); // Allow all variables to be reported during next global definition.
        }

        if (!mainFound) {
            report(new ErrorMessage.MainMissing());
        }

        return null;
    }

    @Override
    public Set<Variable> visit(Bind bind) {
        return bind.lambda.accept(this);
    }


    @Override
    public Set<Variable> visit(LambdaForm lambda) {
        Set<Variable> freeVariables = withScope(lambda.parameters, () -> lambda.body.accept(this));
        freeVariables = without(freeVariables, lambda.parameters); // Find variables that are free in lambda body.

        if (lambda.freeVariables == null) { // If no free variables were given, set the inferred ones.
            lambda.freeVariables = List.copyOf(freeVariables);
        } else {
            // Find (and verify) the variables defined as free.
            Set<Variable> definedFree = lambda.freeVariables.stream().map(this::visit)
                    .flatMap(Set::stream).collect(Collectors.toSet()); // TODO: Warn if there are duplicates in this list?

            // Missing from free are all variables, that are free in body, but are not defined as free and are not a global.
            Set<Variable> missingFromFree = without(freeVariables, definedFree, globalVariables);
            if (!missingFromFree.isEmpty()) {
                report(new ErrorMessage.UndeclaredFreeVariables(lambda, missingFromFree));
            }

            // Unnecessary free are variables defined as free that do not occur in the lambda body.
            Set<Variable> unnecessaryFree = without(definedFree, freeVariables);
            if (!unnecessaryFree.isEmpty()) {
                new WarningMessage.UnnecessaryFreeVariables(lambda, unnecessaryFree).report();
            }
        }

        return freeVariables;
    }


    @Override
    public Set<Variable> visit(LetBinding let) {
        final List<Variable> variables = let.bindings.stream().map(bind -> bind.variable).collect(Collectors.toList());
        final Set<Variable> freeVariables = new HashSet<>();

        if (let.isRecursive) {
            // Every bind can refer to every other. Therefore, we have to introduce them first and check then.
            return withScope(variables, () -> {
                for (Bind bind : let.bindings) {
                    freeVariables.addAll(bind.accept(this));
                }
                freeVariables.addAll(let.expression.accept(this));
                return freeVariables;
            });
        } else {
            for (Bind bind : let.bindings) {
                freeVariables.addAll(bind.accept(this));
            }
            return withScope(variables, () -> {
                freeVariables.addAll(let.expression.accept(this));
                return freeVariables;
            });
        }
    }

    @Override
    public Set<Variable> visit(CaseExpression expression) {
        return union(expression.scrutinized.accept(this),
                expression.alternatives.accept(this));
    }

    private Set<Variable> freeInApplication(Application application) {
        Set<Variable> result = new HashSet<>();
        for (Atom argument : application.arguments) {
            if (argument instanceof Variable variable) {
                result.addAll(variable.accept(this));
            }
        }
        return result;
    }

    @Override
    public Set<Variable> visit(FunctionApplication application) {
        return union(application.function.accept(this), freeInApplication(application));
    }

    @Override
    public Set<Variable> visit(ConstructorApplication application) {
        verifyConstructor(application.constructor, application.arguments.size());
        return freeInApplication(application);
    }

    @Override
    public Set<Variable> visit(PrimitiveApplication application) {
        PrimitiveOperations.getBuiltin(application.operation).ifPresentOrElse(
                (primitiveOperation) -> {
                    if (primitiveOperation.getExpectedParameterCount() != application.arguments.size()) {
                        report(new ErrorMessage.ParameterMismatch(application, primitiveOperation.getExpectedParameterCount()));
                    }
                },
                () -> report(new ErrorMessage.UnknownPrimitive(application))
        );
        return freeInApplication(application);
    }

    @Override
    public Set<Variable> visit(Alternatives alternatives) {
        Set<Variable> freeVariables = new HashSet<>();
        for (Alternative alternative : alternatives) {
            freeVariables.addAll(freeInAlternative(alternative));
        }
        return freeVariables;
    }

    /**
     * @return The free variables within an {@link Alternative}.
     * @implNote Note that this method uses the fact, that {@link Alternative Alternatives}
     * do <b>not</b> return their free variables, but instead return the variables they define
     * for the local scope of their expression.
     */
    private Set<Variable> freeInAlternative(Alternative alternative) {
        final Set<Variable> freeVariables = new HashSet<>();

        final Set<Variable> declaredInAlternative = alternative.accept(this);
        if (alternative.expression != null) {
            // Find free variables used in expression of alternative.
            freeVariables.addAll(withScope(declaredInAlternative, () ->
                    alternative.expression.accept(this)));
        }

        // Variables that were declared in the alternatives scope, are not free.
        freeVariables.removeAll(declaredInAlternative);

        return freeVariables;
    }


    // Alternatives return the Variables that they DEFINE for their local scope.

    @Override
    public Set<Variable> visit(AlgebraicAlternative alternative) {
        verifyConstructor(alternative.constructor, alternative.arguments.size());
        return withScope(alternative.arguments, this::currentScope); // Check for duplicate definitions and return scope.
    }

    @Override
    public Set<Variable> visit(DefaultBindingAlternative alternative) {
        return Set.of(alternative.variable);
    }

    @Override
    public Set<Variable> visit(DefaultFallthroughAlternative alternative) {
        return Collections.emptySet();
    }

    @Override
    public Set<Variable> visit(NoAlternative noAlternative) {
        return Collections.emptySet();
    }

    @Override
    public Set<Variable> visit(PrimitiveAlternative alternative) {
        return Collections.emptySet();
    }


    @Override
    public Set<Variable> visit(Variable variable) {
        if (!isDefined(variable) && !isFirstReport(variable)) {
            // Only report first occurrence of undefined variable.
            report(new ErrorMessage.UnknownVariable(variable, visibleVariables()));
        }
        return Set.of(variable);
    }

    @Override
    public Set<Variable> visit(Literal literal) {
        return Collections.emptySet();
    }

    @Override
    public Set<Variable> visit(Constructor constructor) {
        throw new IllegalStateException("Method should not be called.");
    }

}
