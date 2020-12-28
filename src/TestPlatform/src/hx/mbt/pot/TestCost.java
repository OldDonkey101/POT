package hx.mbt.pot;

import hx.mbt.fsm.InputSequence;

import java.util.Set;

public interface TestCost {
    public int costGrowth(Set<InputSequence> TS, InputSequence seqToAdd);
    public int costGrowth(Set<InputSequence> TS, Set<InputSequence> seqSetToAdd);
}