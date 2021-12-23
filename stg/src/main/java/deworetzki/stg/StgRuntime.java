package deworetzki.stg;

import deworetzki.stg.analysis.Analysis;
import deworetzki.stg.semantic.Code;
import deworetzki.stg.semantic.Machine;
import deworetzki.stg.syntax.Program;

import java.util.NoSuchElementException;

public record StgRuntime(Options options) {

    public void run(final Program program) {
        Analysis analysis = Analysis.runOn(program, options);
        if (!analysis.hasReportedErrors()) {
            final Machine machine = new Machine(program);
            try {
                while (true) {
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

        // TODO: Add CLI to machine?
    }
}
