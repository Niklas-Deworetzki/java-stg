package deworetzki.stg.semantic;

import java.util.Deque;

/**
 * An {@link UpdateFrame} is a data class with a single method to restore its contents to a machine.
 */
public class UpdateFrame {
    private final Deque<Value> argumentStack;
    private final Deque<Continuation> returnStack;
    private final int address;

    public UpdateFrame(Deque<Value> argumentStack, Deque<Continuation> returnStack, int address) {
        this.argumentStack = argumentStack;
        this.returnStack = returnStack;
        this.address = address;
    }

    public void update(Machine machine, Closure closure) {
        // Restore argument and return stack.
        machine.getArgumentStack().addAll(argumentStack);
        machine.getReturnStack().addAll(returnStack);

        // Update address with a new closure
        machine.getHeap().update(address, closure);
    }
}
