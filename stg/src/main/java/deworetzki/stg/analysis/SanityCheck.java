package deworetzki.stg.analysis;

import deworetzki.messages.ErrorMessage;
import deworetzki.stg.Options;
import deworetzki.stg.syntax.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SanityCheck extends Analysis {

    @Override
    public void check(Options options, Program program) {
        validateBinds(program.bindings);
    }


    private void validateExpression(final Expression expression) {
        switch (expression) {
            case (CaseExpression caseExpression) -> {
                validateExpression(caseExpression.scrutinized);
                for (Alternative alternative : caseExpression.alternatives) {
                    validateAlternative(alternative);
                }
            }
            case (LetBinding let) -> {
                validateBinds(let.bindings);
                validateExpression(let.expression);
            }
            default -> {
            }
        }
    }

    private void validateBinds(final List<Bind> bindings) {
        final Set<Variable> declaredVariables = new HashSet<>();

        for (Bind bind : bindings) {
            if (declaredVariables.contains(bind.variable)) {
                reportError(new ErrorMessage.NameCollision(bind));
                return;
            }

            declaredVariables.add(bind.variable);
        }

        for (Bind bind : bindings) {
            validateExpression(bind.lambda.body);
        }
    }

    private void validateAlternative(final Alternative alternative) {

        validateExpression(alternative.expression);
    }
}
