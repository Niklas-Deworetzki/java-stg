package deworetzki.stg.semantic;

import java.util.Deque;
import java.util.function.UnaryOperator;

public class UpdateFrame {
    private final Deque<Value> argumentStack;
    private final Deque<Continuation> returnStack;
    private final int address;

    public UpdateFrame(Deque<Value> argumentStack, Deque<Continuation> returnStack, int address) {
        this.argumentStack = argumentStack;
        this.returnStack = returnStack;
        this.address = address;
    }

    public void update(Machine machine, UnaryOperator<Closure> update) {
        // Restore argument and return stack.
        machine.getArgumentStack().addAll(argumentStack);
        machine.getReturnStack().addAll(returnStack);

        // Update address with a new closure
        machine.getHeap().update(address, update.apply(machine.getHeap().get(address)));
    }
}
