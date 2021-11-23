package deworetzki.stg.semantic;

import deworetzki.stg.syntax.LambdaForm;

import java.util.List;

public record Closure(LambdaForm code, List<Value> values) {
}
