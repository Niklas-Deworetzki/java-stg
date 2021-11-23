package deworetzki.stg.semantic;

import deworetzki.parse.Position;
import deworetzki.stg.syntax.Expression;
import deworetzki.stg.syntax.Program;
import deworetzki.stg.syntax.Variable;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

public class Machine {
    public static final Expression ENTRY_POINT = new Variable(Position.NONE, "main");

    private Code code = new Code.Eval(ENTRY_POINT, Collections.emptyMap());
    private final Deque<Value> argumentStack = new LinkedList<>();
    private final Deque<Continuation> returnStack = new LinkedList<>();
    private final Deque<UpdateFrame> updateStack = new LinkedList<>();
    private final Heap heap = new Heap();
    private final Map<Variable, Value> globalEnvironment;


    public Machine(Program program) {
        this.globalEnvironment = heap.initialize(program);
    }
}
