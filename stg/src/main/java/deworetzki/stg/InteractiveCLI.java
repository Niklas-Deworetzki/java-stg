package deworetzki.stg;

import deworetzki.stg.semantic.Code;
import deworetzki.stg.semantic.Continuation;
import deworetzki.stg.semantic.Machine;
import deworetzki.stg.semantic.UpdateFrame;
import deworetzki.stg.syntax.*;

import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class InteractiveCLI implements Runnable {
    private final Machine machine;


    public InteractiveCLI(Machine machine) {
        this.machine = machine;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                showState();
                //scanner.nextLine();
                machine.step();
            }
        } catch (NoSuchElementException returnFromExecution) {
            final Code code = machine.getCode();
            if (code instanceof Code.ReturnConstructor ret) {
                System.out.println(ret.constructor());
            } else if (code instanceof Code.ReturnInteger ret) {
                System.out.println(ret.integer());
            } else {
                System.out.printf("Error in state %s: %s%n", code, returnFromExecution.getMessage());
            }
        }
    }

    public void showState() {
        System.out.printf("State: %s%n", showCodeState(machine.getCode()));
        System.out.printf("Arguments: %s%n", showStack(machine.getArgumentStack(), String::valueOf, 7));
        System.out.printf("Return To: %s%n", showStack(machine.getReturnStack(), InteractiveCLI::showContinuation, 3));
        System.out.printf("Updates: %s%n", showStack(machine.getUpdateStack(), InteractiveCLI::showUpdateFrame, 1));
    }

    private static String showUpdateFrame(UpdateFrame frame) {
        return String.format("(%s | (%s) | @%s)",
                showStack(frame.argumentStack(), String::valueOf, 3),
                frame.returnStack().size(),
                frame.address());
    }

    private static String showContinuation(Continuation continuation) {
        return Integer.toString(continuation.alternatives().position.getLine());
    }

    private static String showCodeState(Code code) {
        if (code instanceof Code.Eval eval) {
            return String.format("Eval %s", showExpression(eval.expression()));

        } else if (code instanceof Code.Enter enter) {
            return String.format("Enter @%d", enter.address());

        } else if (code instanceof Code.ReturnConstructor ret) {
            return "Return " + ret.constructor();

        } else if (code instanceof Code.ReturnInteger ret) {
            return "Return " + ret.integer();
        }

        return code.getClass().getSimpleName();
    }

    private static String showExpression(Expression expression) {
        if (expression instanceof CaseExpression caseExpression) {
            return "case " + showExpression(caseExpression.scrutinized);
        } else if (expression instanceof LetBinding let) {
            String prefix = (let.isRecursive) ? "letrec" : "let";
            return prefix + let.bindings.stream().map(bind -> bind.variable.name).collect(Collectors.joining(" ", "{", "}"));
        } else if (expression instanceof Literal literal) {
            return literal.toString();
        } else if (expression instanceof Application application) {
            String function = "???";
            if (application instanceof FunctionApplication app) {
                function = app.function.name;
            } else if (application instanceof ConstructorApplication app) {
                function = app.constructor.name;
            } else if (application instanceof PrimitiveApplication app) {
                function = app.operation;
            }
            return function + application.arguments.stream().map(Object::toString).collect(Collectors.joining(" ", " (", ")"));
        }
        return expression.toString();
    }

    private static <E> String showStack(Deque<E> stack, Function<E, String> showElement, int cutoff) {
        String show = stack.stream().limit(cutoff).map(showElement).collect(Collectors.joining(" "));

        if (stack.size() > cutoff) {
            show += " ... ";
        }

        return show + String.format(" (%d)", stack.size());
    }
}
