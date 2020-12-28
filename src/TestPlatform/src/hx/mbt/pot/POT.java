package hx.mbt.pot;

import hx.mbt.fsm.*;

import javax.swing.*;
import java.util.*;

public class POT {

    public TestSuite deriveCompleteTSOfConf(MealyMachine spec, int m, TestCost testCost, int optimizeMode) {
        HomoMapping mapping = new HomoMapping(spec, spec);
        for (State s : spec.getStateSet()) {
            mapping.addMapping(s, s);
        }
        return deriveCompleteTSOfPOT(mapping, m, testCost, optimizeMode);
    }

    public TestSuite deriveCompleteTSOfPOT(HomoMapping mapping, int m, TestCost testCost, int optimizeMode) {
//        mapping.reduce();
        InputSequence proxy = new InputSequence();
        MealyMachine spec = mapping.getS();
        FSM property = mapping.getA();
        //n
        int n = spec.getStateSet().size();
        //X
        Set<Input> X = spec.getInputSet();
        //R
        RMatrix R = new RMatrix(property);

        //Compute Q: state cover of S
        Set<InputSequence> Q = spec.getStateCover();
        //P=Q \cup Q.X[m-n]
        Set<InputSequence> P = new HashSet<>();
        P.addAll(Q);
        P.addAll(proxy.concatTwoSequenceSet(Q,proxy.getCupPowerSet(X,m - n)));

        //TS = Q \cup Q.X[m-n+1]
        TestSuite TS = new TestSuite();
        TS.addAllInputSequences(proxy.concatTwoSequenceSet(Q, proxy.getCupPowerSet(X, m - n+1)));

        //Q X Q
        Set<InputSequencePair> visitedPair = new HashSet<>();
        for (InputSequence alpha1 : Q) {
            for (InputSequence alpha2 : Q) {
                if (!visitedPair.contains(new InputSequencePair(alpha2, alpha1))) {
                    visitedPair.add(new InputSequencePair(alpha1, alpha2));
                    State s1 = spec.reachableState(spec.getInitialState(), alpha1);
                    State s2 = spec.reachableState(spec.getInitialState(), alpha2);
                    if (!s1.equals(s2)) {
                        //Find the best lambda
                        InputSequence lambda = getBestSepSeq(alpha1, s1, alpha2, s2, spec, TS, testCost,optimizeMode);
                        TS.addInputSequence(Sequence.concatenateTwoSequence(alpha1, lambda));
                        TS.addInputSequence(Sequence.concatenateTwoSequence(alpha2, lambda));
                    }
                }
            }
        }

        //P X Q
        for (InputSequence gamma1 : P) {
            for (InputSequence gamma2 : Q) {
                for (Input x : spec.getInputSet()) {
                    State s1 = spec.postState(spec.getInitialState(), gamma1);
                    State s2 = spec.postState(spec.getInitialState(), gamma2);
                    Output y = spec.getOutput(s1, x);
                    State a1 = mapping.getAState(s1);
                    State a2 = mapping.getAState(s2);
                    Set<State> al = property.transitToWithOutput(a1, x, y);
                    boolean allSatisfy = true;
                    for (State a : al) {
                        if (R.get(a2, a) != -1) {
                            allSatisfy = false;
                            break;
                        }
                    }
                    if (allSatisfy) {
                        InputSequence gammax = Sequence.concatenateTwoSequence(gamma1, x);
                        if (!visitedPair.contains(new InputSequencePair(gamma2,gammax))) {
                            visitedPair.add(new InputSequencePair(gammax, gamma2));
                            InputSequence lambda = getBestSepSeq(gammax, null, gamma2, s2, spec, TS, testCost, optimizeMode);
                            TS.addInputSequence(Sequence.concatenateTwoSequence(gammax, lambda));
                            TS.addInputSequence(Sequence.concatenateTwoSequence(gamma2, lambda));
                        }
                    }
                }
            }
        }

        Set<InputSequence> E = proxy.getCupPowerSet(X, m - n);
        for (InputSequence alpha : Q) {
            for (InputSequence beta: E) {
                for (int i = 0; i < beta.getLength(); i++) {
                    for (int j = 0; j < beta.getLength(); j++) {
                        for (Input x : spec.getInputSet()) {
                            InputSequence alpha1 = Sequence.concatenateTwoSequence(alpha, beta.subSequence(0, i + 1));
                            InputSequence alpha2 = Sequence.concatenateTwoSequence(alpha, beta.subSequence(0, j + 1));
                            State s1 = spec.reachableState(spec.getInitialState(), alpha1);
                            State s2 = spec.reachableState(spec.getInitialState(), alpha2);
                            Output y = spec.getOutput(s1, x);
                            State a1 = mapping.getAState(s1);
                            State a2 = mapping.getAState(s2);
                            Set<State> al = property.transitToWithOutput(a1, x, y);
                            boolean allSatisfy = true;
                            for (State a : al) {
                                if (R.get(a2, a) != -1) {
                                    allSatisfy = false;
                                    break;
                                }
                            }
                            if (allSatisfy) {
                                InputSequence alpha1x = Sequence.concatenateTwoSequence(alpha1, x);
                                if (!visitedPair.contains(new InputSequencePair(alpha1x,alpha2))
                                        && !visitedPair.contains(new InputSequencePair(alpha2,alpha1x))) {
                                    visitedPair.add(new InputSequencePair(alpha1x, alpha2));
                                    InputSequence lambda = getBestSepSeq(alpha1x, null, alpha2, s2, spec, TS, testCost, optimizeMode);
                                    TS.addInputSequence(Sequence.concatenateTwoSequence(alpha1x, lambda));
                                    TS.addInputSequence(Sequence.concatenateTwoSequence(alpha2, lambda));
                                }
                            }
                        }

                    }
                }
            }
        }
        return TS;
    }

    public InputSequence getBestSepSeq(InputSequence alpha1, State s1, InputSequence alpha2,State s2, MealyMachine M, TestSuite TS, TestCost testCost, int optimizeMode) {

        InputSequence bestLambda = new InputSequence();

        //Step: check para's validity.
        assert(alpha1 != null && alpha2 != null && M!=null && TS!=null);
        if (s1 == null) {
            s1 = M.reachableState(M.getInitialState(), alpha1);
        }
        if (s2 == null) {
            s2 = M.reachableState(M.getInitialState(), alpha2);
        }


        //Step1: Find candidate lambda for s1 and s2, including:
        //A. shortest lambdas
        //B. alpha1.lambda or alpha2.lambda in TS.
        //C. shortest lambda such that either alpha1.lambda or alpha2.lambda has a prefix in partial(TS)
        Set<InputSequence> candSepSeqs = new HashSet<>();

        //shortest lambdas
        ArrayList<Partition> rhoPartitions;
        rhoPartitions = M.getRhoPartitions();

        if (optimizeMode == 0) {
            bestLambda = M.getSepSeqSet().get(new MealyMachine.StatePair(s1, s2)).iterator().next();
            return bestLambda;
        }
        if (optimizeMode == 1) {

            if (!M.containsNoSepSeq(alpha1, s1, alpha2, s2, TS.getInputSeqSet())) {
                bestLambda = M.getSepSeqSet().get(new MealyMachine.StatePair(s1, s2)).iterator().next();
            } else {
                bestLambda = new InputSequence();
            }
            return bestLambda;
        }
        if (optimizeMode == 2) {
            candSepSeqs = M.getSepSeqSet().get(new MealyMachine.StatePair(s1, s2));
        }
        if (optimizeMode == 3) {
            candSepSeqs.addAll(M.getSepSeqSet().get(new MealyMachine.StatePair(s1, s2)));

            //alpha1.lambda or alpha2.lambda in TS.
            //sequences in TS starting with alpha1 or alpha2
            //remove their prefixes (alpha1 or alpha2)
            Collection<InputSequence> seqsAfterAlpha = new HashSet<>();
            Set<InputSequence> partialTS = Sequence.partialSet(TS.getInputSeqSet());
            for (InputSequence inSeq: partialTS) {
                if (inSeq.hasPrefix(alpha1) || inSeq.hasPrefix(alpha2)) {
                    seqsAfterAlpha.add(inSeq.removePrefix(alpha1));
                }
            }
            //check whether it is sepSeq.
            for (InputSequence inSeq : seqsAfterAlpha) {
                if (M.isSepSeq(s1, s2, inSeq)) {
                    candSepSeqs.add(M.getShortestSepSeqPrefix(s1, s2, inSeq));
                }
            }

//            //shortest lambda such that either alpha1.lambda or alpha2.lambda has a prefix in partial(TS)
//            Set<InputSequence> partialTS = Sequence.partialSet(TS.getInputSeqSet());
//            Collection<InputSequence> seqStartsAlphaNoSeq = new HashSet<>();
//            for (InputSequence inSeq: partialTS) {
//                InputSequence tail = null;
//                if (inSeq.hasPrefix(alpha1)){
//                    tail = inSeq.removePrefix(alpha1);
//                }
//                else if (inSeq.hasPrefix(alpha1)) {
//                    tail = inSeq.removePrefix(alpha2);
//                }
//                if (tail != null) {
//                    if (!M.isSepSeq(s1, s2, tail)) {
//                        State s1Post = M.postState(s1, inSeq);
//                        State s2Post = M.postState(s2, inSeq);
//                        for (InputSequence sepSeq : M.getSepSeqSet(s1Post, s2Post, rhoPartitions)) {
//                            candSepSeqs.add(Sequence.concatenateTwoSequence(inSeq, sepSeq));
//                        }
//                    }
//                }
//            }
        }




        //Step2: Select the lambda with minimal cost
        int cost = 10000;

        for (InputSequence lambda : candSepSeqs) {
            InputSequence seq1 = Sequence.concatenateTwoSequence(alpha1, lambda);
            InputSequence seq2 = Sequence.concatenateTwoSequence(alpha2, lambda);
            int tmpCost =testCost.costGrowth(TS.getInputSeqSet(), new HashSet<InputSequence>(){{add(seq1);add(seq2);}});
            if (tmpCost < cost) {
                cost = tmpCost;
                bestLambda = lambda;
            }
        }

        return bestLambda;
    }
}
