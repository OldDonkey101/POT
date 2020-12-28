package hx.mbt.pot;

import hx.mbt.fsm.*;
import hx.mbt.pot.TestSuite;

import java.util.ArrayList;
import java.util.Set;

public class HMethod {
    public TestSuite deriveCompleteTS(MealyMachine spec, int m) {
        InputSequence proxy = new InputSequence();
        //n
        int n = spec.getStateSet().size();
        //X
        Set<Input> X = spec.getInputSet();
        //Q
        Set<InputSequence> Q = spec.getStateCover();
        //TS = Q.X[m-n+1]
        TestSuite TS = new TestSuite();
        TS.addAllInputSequences(proxy.concatTwoSequenceSet(Q, proxy.getCupPowerSet(X, m - n+1)));

        ArrayList<Partition> rhoPartitions;
        rhoPartitions = spec.getRhoPartitions();

        //Step2.
        //Q X Q
        for (InputSequence alpha1 : Q) {
            for (InputSequence alpha2 : Q) {
                State s1 = spec.reachableState(spec.getInitialState(), alpha1);
                State s2 = spec.reachableState(spec.getInitialState(), alpha2);
                if (!s1.equals(s2)) {
                    if (!spec.containsNoSepSeq(alpha1, s1, alpha2, s2, TS.getInputSeqSet())) {
                        Set<InputSequence> sepSeqSet = spec.getSepSeqSet(s1, s2, rhoPartitions);
                        InputSequence lambda = sepSeqSet.iterator().next();
                        TS.addInputSequence(Sequence.concatenateTwoSequence(alpha1, lambda));
                        TS.addInputSequence(Sequence.concatenateTwoSequence(alpha2, lambda));
                    }

                }
            }
        }
        //Step3.
        Set<InputSequence> P = proxy.concatTwoSequenceSet(Q, proxy.getCupPowerSet(X, m - n+1));
        //P X Q
        for (InputSequence alpha1 : P) {
            for (InputSequence alpha2 : Q) {
                State s1 = spec.reachableState(spec.getInitialState(), alpha1);
                State s2 = spec.reachableState(spec.getInitialState(), alpha2);
                if (!s1.equals(s2)) {
                    if (!spec.containsNoSepSeq(alpha1, s1, alpha2, s2, TS.getInputSeqSet())) {
                        Set<InputSequence> sepSeqSet = spec.getSepSeqSet(s1, s2, rhoPartitions);
                        InputSequence lambda =  sepSeqSet.iterator().next();
                        TS.addInputSequence(Sequence.concatenateTwoSequence(alpha1, lambda));
                        TS.addInputSequence(Sequence.concatenateTwoSequence(alpha2, lambda));
                    }
                }
            }
        }
        //Step4.
        Set<InputSequence> E = proxy.getCupPowerSet(X, m - n+1);
        for (InputSequence alpha : Q) {
            for (InputSequence beta: E) {
                for (int i = 0; i < beta.getLength(); i++) {
                    for (int j = i + 1; j < beta.getLength(); j++) {
                        InputSequence alpha1 = Sequence.concatenateTwoSequence(alpha,beta.subSequence(0, i + 1));
                        InputSequence alpha2 = Sequence.concatenateTwoSequence(alpha,beta.subSequence(0, j + 1));
                        State s1 = spec.reachableState(spec.getInitialState(), alpha1);
                        State s2 = spec.reachableState(spec.getInitialState(), alpha2);
                        if (!s1.equals(s2)) {
                            if (!spec.containsNoSepSeq(alpha1, s1, alpha2, s2, TS.getInputSeqSet())) {
                                Set<InputSequence> sepSeqSet = spec.getSepSeqSet(s1, s2, rhoPartitions);
                                InputSequence lambda = sepSeqSet.iterator().next();
                                TS.addInputSequence(Sequence.concatenateTwoSequence(alpha1, lambda));
                                TS.addInputSequence(Sequence.concatenateTwoSequence(alpha2, lambda));
                            }
                        }
                    }
                }
            }
        }
        return TS;
    }
}
