package deworetzki.stg;

import deworetzki.stg.analysis.Analysis;
import deworetzki.stg.semantic.Machine;
import deworetzki.stg.syntax.Program;

public record StgRuntime(Options options) {

    public void run(final Program program) {
        Analysis analysis = Analysis.runOn(program, options);
        if (!analysis.hasReportedErrors()) {
            final Machine machine = new Machine(program);
            new InteractiveCLI(machine).run();

        }
    }
}
