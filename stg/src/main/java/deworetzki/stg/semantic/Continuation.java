package deworetzki.stg.semantic;

import deworetzki.stg.syntax.Alternatives;
import deworetzki.stg.syntax.Variable;

import java.util.Map;

// TODO
public record Continuation(Alternatives alternatives, Map<Variable, Value> savedEnvironment) {

}
