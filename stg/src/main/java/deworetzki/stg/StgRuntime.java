package deworetzki.stg;

import deworetzki.stg.analysis.Analysis;
import deworetzki.stg.semantic.Closure;
import deworetzki.stg.semantic.Machine;
import deworetzki.stg.syntax.*;

/**
 * The {@link StgRuntime} record provides a simple driver interface, to run a {@link Program}.
 */
public record StgRuntime(Options options) {

    public void analyzeAndRun(final Program program) {
        Analysis analysis = Analysis.runOn(program, options);
        if (!analysis.hasReportedErrors()) {
            final Machine machine = new Machine(program);
            final Closure result = machine.run();

            System.out.println(formatClosure(result));
        }
    }

    private static String formatClosure(Closure closure) {
        final LambdaForm code = closure.code();

        if (code.parameters.size() > 0) {
            return "Function";
        } else if (code.body instanceof ConstructorApplication application) {
            return application.constructor.name;
        } else if (code.body instanceof Literal literal) {
            return Integer.toString(literal.value);
        } else {
            // Result is not valid. We try our best to create a representation.
            return code.toString();
        }
    }
}
