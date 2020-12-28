package hx.mbt.fsm;

import java.util.HashSet;
import java.util.Set;

public class Block{
    Set<State> states;
    public Block() {
        states = new HashSet<>();
    }
    public boolean contains(State s) {
        return states.contains(s);
    }

    public Set<State> getStates() {
        return states;
    }

    public void addState(State state) {
        this.states.add(state);
    }

    public boolean containAll(Block block) {
        return states.containsAll(block.getStates());
    }
}
