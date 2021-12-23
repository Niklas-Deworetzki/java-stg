package deworetzki.stg.semantic;

import java.util.Deque;

public record UpdateFrame(Deque<Value> argumentStack, Deque<Continuation> returnStack, int address) {
}
