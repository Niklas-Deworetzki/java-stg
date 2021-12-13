package deworetzki.stg.analysis;

import deworetzki.messages.ErrorMessage;
import deworetzki.stg.Options;
import deworetzki.stg.syntax.Program;

import java.util.List;

public abstract class Analysis {
    private boolean reportedError = false;

    protected final void reportError(final ErrorMessage errorMessage) {
        reportedError = true;
        errorMessage.report();
    }

    public final boolean hasReportedErrors() {
        return reportedError;
    }


    public abstract void check(final Options options, final Program program);

    public static void performAll(final Options options, final Program program) throws ErrorMessage {
        final List<Analysis> analysisImplementations = List.of(

        );

        for (Analysis analysisPass : analysisImplementations) {
            analysisPass.check(options, program);
            if (analysisPass.hasReportedErrors()) {
                throw new ErrorMessage.AnalysisTerminated();
            }
        }
    }
}
