package hx.mbt.pot;

import hx.mbt.fsm.InputSequence;
import hx.mbt.fsm.Sequence;

import java.util.Set;

public class TestCostTrivialReset implements TestCost{
    @Override
    public int costGrowth(Set<InputSequence> TS, InputSequence seqToAdd) {
        //If seqToAdd in Pref(TS), return 0.
        if (seqToAdd.isPrefixOf(TS)) {
            return 0;
        }
        //If seqToAdd has a prefix in partialTS.
        Set<InputSequence> partialTS = Sequence.partialSet(TS);
        int cost = seqToAdd.getLength();
        for (InputSequence seq : partialTS) {
            if (seq.isPrefixOf(seqToAdd)) {
                int tmp = seqToAdd.getLength() - seq.getLength();
                cost = tmp<cost?tmp:cost;
            }
        }

        //Other conditions
        if (cost == seqToAdd.getLength()) {
            cost++;
        }
        return cost;
    }

    @Override
    public int costGrowth(Set<InputSequence> TS, Set<InputSequence> seqSetToAdd) {
        int cost = 0;
        for (InputSequence seqToAdd : seqSetToAdd) {
            cost += costGrowth(TS, seqToAdd);
            TS.add(seqToAdd);
        }
        return cost;
    }
}
