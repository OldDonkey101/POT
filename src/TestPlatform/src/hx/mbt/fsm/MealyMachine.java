package hx.mbt.fsm;

import hx.mbt.pot.FileUtil;
import jxl.read.biff.BiffException;

import java.io.IOException;
import java.util.*;

public class MealyMachine extends FSM {

    public static class StatePair {
        private State s1;
        private State s2;

        public StatePair(State s1, State s2) {
            this.s1 = s1;
            this.s2 = s2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StatePair statePair = (StatePair) o;
            return (s1.equals(statePair.s1) && s2.equals(statePair.s2)) ||
                    (s1.equals(statePair.s2) && s2.equals(statePair.s1));
        }

        @Override
        public int hashCode() {
            return s1.hashCode() + s2.hashCode();
        }
    }

    private Map<StatePair, Set<InputSequence>> sepSeqSet;

    public Map<StatePair, Set<InputSequence>> getSepSeqSet() {
        return sepSeqSet;
    }

    public void buildSepSeqSet() {
        sepSeqSet = new HashMap<>();
        ArrayList<Partition> rhoPartions = getRhoPartitions();
        ArrayList<State> stateList = new ArrayList<>(getStateSet());
        for (int i = 0; i< getStateSet().size(); i++) {
            for (int j = i+1; j < getStateSet().size(); j++) {
                StatePair statePair = new StatePair(stateList.get(i),stateList.get(j));
                sepSeqSet.put(statePair, getSepSeqSet(stateList.get(i), stateList.get(j), rhoPartions));
            }
        }
    }

    public MealyMachine(Set<Input> inputSet, Set<Output> outputSet, Set<State> stateSet, State initialState, Set<Transition> transitionSet) {
        super(inputSet, outputSet, stateSet, initialState, transitionSet);
    }

    public OutputSequence getOutputSeq(State s, InputSequence inSeq) {
        return getVisitTree(s, inSeq).getOutputSequences().iterator().next();
    }

    public Output getOutput(State s, Input input) {
        return outputs(s, input).iterator().next();
    }

    public State postState(State s, Input input) {
        return transitTo(s, input).iterator().next();
    }

    public State postState(State s, InputSequence inSep) {
        return getVisitTree(s, inSep).getReachableState().iterator().next();
    }

    public Set<InputSequence> getStateCover() {
        Set<InputSequence> Q = new HashSet<>();
        //重置访问状态
        this.clearVisitStatus();
        //重置当前状态
        this.resetCurrentState();
        //待访问状态和对应的输入序列
        Stack<State> visitedStates = new Stack<>();
        Stack<InputSequence> usedInputSequences = new Stack<>();
        visitedStates.push(this.getCurrentState());
        this.getCurrentState().visit();
        usedInputSequences.push(new InputSequence());//空序列

        while (!visitedStates.isEmpty()) {
            State visitedState = visitedStates.pop();
            InputSequence inputSequence = usedInputSequences.pop();
            Q.add(inputSequence);
            Collection<Transition> leavingTransitions = this.getTransitionsToOtherState(visitedState);
            Iterator<Transition> transitionIterator = leavingTransitions.iterator();
            while (transitionIterator.hasNext()) {
                DTransition leavingTrans = (DTransition) transitionIterator.next();
                if (!leavingTrans.getDesState().isVisited()) {
                    visitedStates.push(leavingTrans.getDesState());
                    leavingTrans.getDesState().visit();
                    InputSequence newSequence = inputSequence.copy();
                    newSequence.concatenateBy(leavingTrans.getInput());
                    usedInputSequences.push(newSequence);
                }
            }
        }
        return Q;
    }

    @Override
    public FSM copy() {
        Set<Input> inputSet = new HashSet<>();
        for (Input input : getInputSet()) {
            inputSet.add(new Input(input.getSignature()));
        }
        Set<Output> outputSet = new HashSet<>();
        for (Output output : getOutputSet()) {
            outputSet.add(new Output(output.getSignature()));
        }
        Set<State> stateSet = new HashSet<>();
        for (State state : getStateSet()) {
            stateSet.add(new State(state.getSignature()));
        }
        Set<Transition> transitionSet = new HashSet<>();
        for (Transition transition : getTransitionSet()) {
            transitionSet.add(transition.copy());
        }
        return new MealyMachine(inputSet, outputSet, stateSet, new State(getInitialState().getSignature()), transitionSet);
    }

    public State reachableState(State srcState, Input input) {
        return this.transitTo(srcState, input).iterator().next();
    }

    public State reachableState(State srcState, InputSequence inputSequence) {
        return this.getVisitTree(srcState, inputSequence).getLeafNodes().iterator().next().getState();
    }

    public boolean isSepSeq(State s1, State s2, InputSequence seq) {
        return !getOutputSeq(s1, seq).equals(getOutputSeq(s2, seq));
    }


    public boolean haveSameOutput(State s1, State s2) {
        for (Input in : getInputSet()) {
            Output o1 = getOutput(s1, in);
            Output o2 = getOutput(s2, in);
            if (!o1.equals(o2)) {
                return false;
            }
        }
        return true;
    }

    public boolean haveSameOutput(State state, Block block) {
        for (State s2 : block.getStates()) {
            if (!haveSameOutput(state, s2)) {
                return false;
            }
        }
        return true;
    }

    public boolean inSameBlock(State s1, State s2, Partition partition) {
        for (Block block : partition.getBlocks()) {
            if (block.contains(s1)) {
                if (block.contains(s2)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean postStateInSameBlock(State s1, Block block, Partition partition) {
        for (Input in : getInputSet()) {
            State postOfs1 = postState(s1, in);
            for (State s2 : block.getStates()) {
                State postOfs2 = postState(s2, in);
                if (!inSameBlock(postOfs1, postOfs2, partition)) {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<Partition> getRhoPartitions() {
        ArrayList<Partition> rhoPartitions = new ArrayList<Partition>();
        //建立rho1
        Partition p1 = new Partition();
        for (State s : getStateSet()) {
            boolean needNewBlock = true;
            for (Block block : p1.blocks) {
                if (haveSameOutput(s, block)) {
                    block.addState(s);
                    needNewBlock = false;
                    break;
                }
            }
            if (needNewBlock) {
                Block newBlock = new Block();
                newBlock.states.add(s);
                p1.addBlock(newBlock);
            }
        }

        rhoPartitions.add(p1);
        boolean need2Refine = p1.blocks.size() != getStateSet().size();
        while (need2Refine) {
            Partition pi = new Partition();
            Partition oldPartition = rhoPartitions.get(rhoPartitions.size() - 1);
            for (Block oldBlock : oldPartition.getBlocks()) {
                for (State s : oldBlock.getStates()) {
                    boolean needNewBlock = true;
                    for (Block block : pi.blocks) {
                        if (oldBlock.containAll(block)) {
                            if (postStateInSameBlock(s, block, oldPartition)) {
                                block.addState(s);
                                needNewBlock = false;
                                break;
                            }
                        }
                    }
                    if (needNewBlock) {
                        Block newBlock = new Block();
                        newBlock.states.add(s);
                        pi.addBlock(newBlock);
                    }
                }
            }

            if (pi.blocks.size() == oldPartition.blocks.size()) {
                need2Refine = false;
            } else {
                rhoPartitions.add(pi);
            }
        }
        return rhoPartitions;
    }

    public InputSequence getShortestSepSeqPrefix(State s1, State s2, InputSequence sepSeq) {
        //TODO: 效率低
        assert (isSepSeq(s1, s2, sepSeq));
        for (int i = 1; i <= sepSeq.getLength(); i++) {
            InputSequence prefix = sepSeq.getPrefix(i);
            if (isSepSeq(s1, s2, prefix)) {
                return prefix;
            }
        }
        return sepSeq;
    }


    public Set<InputSequence> getSepSeqSet(State s1, State s2, ArrayList<Partition> rhoPartitions) {
        if (s1.equals(s2)) {
            return new HashSet<InputSequence>() {{
                add(new InputSequence());
            }};
        }
        //得到最短长度的lambda的集合
        Set<InputSequence> sepSeqSet = new HashSet<>();
        for (int i = 0; i < rhoPartitions.size(); i++) {
            if (!inSameBlock(s1, s2, rhoPartitions.get(i))) {
                if (i == 0) {
                    // length of 1
                    for (Input in : getInputSet()) {
                        if (!getOutput(s1, in).equals(getOutput(s2, in))) {
                            InputSequence sepSeq = InputSequence.createEmptyInputSeq();
                            sepSeq.concatenateBy(in);
                            sepSeqSet.add(sepSeq);
                        }
                    }

                } else {
                    //length of i+1
                    //找到将poststate带入rhoi-1中不同block的输入
                    for (Input in : getInputSet()) {
                        State s1PostState = postState(s1, in);
                        State s2PostState = postState(s2, in);
                        if (!inSameBlock(s1PostState, s2PostState, rhoPartitions.get(i - 1))) {
                            for (InputSequence inSeq : getSepSeqSet(s1PostState, s2PostState, rhoPartitions)) {
                                InputSequence sepSeq = InputSequence.createEmptyInputSeq();
                                sepSeq.concatenateBy(in);
                                sepSeq.concatenateBy(inSeq);
                                sepSeqSet.add(sepSeq);
                            }
                        }
                    }

                }
                break;
            }
        }
        return sepSeqSet;
    }

    public boolean containsNoSepSeq(InputSequence alpha1, State s1, InputSequence alpha2, State s2, Set<InputSequence> sepSet) {
        if (s1 == null) {
            s1 = postState(this.getInitialState(), alpha1);
        }
        if (s2 == null) {
            s2 = postState(this.getInitialState(), alpha2);
        }
        //Find all sequences starts with alpha1 and alpha2
        Set<InputSequence> sepsStartWithAlpha1 = new HashSet<>();
        Set<InputSequence> sepsStartWithAlpha2 = new HashSet<>();
        for (InputSequence sep : sepSet) {
            if (sep.hasPrefix(alpha1)) {
                sepsStartWithAlpha1.add(sep.copy().removePrefix(alpha1));
            }
            if (sep.hasPrefix(alpha2)) {
                sepsStartWithAlpha2.add(sep.copy().removePrefix(alpha2));
            }
        }
        for (InputSequence afterAlpha1 : sepsStartWithAlpha1) {
            for (int i = 1; i < afterAlpha1.getLength(); i++) {
                InputSequence commonPrefix = afterAlpha1.subSequence(0, i);
                //check if this prefix is a sep
                if (isSepSeq(s1, s2, commonPrefix)) {
                    //check if this prefix is a common prefix
                    boolean isCommonPrefix = false;
                    for (InputSequence afterAlpha2 : sepsStartWithAlpha2) {
                        if (afterAlpha2.hasPrefix(commonPrefix)) {
                            isCommonPrefix = true;
                            break;
                        }
                    }
                    if (isCommonPrefix) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public MealyMachine minimize(Map<State, State> stateMap) {
        //partition states into blocks
        ArrayList<Partition> partitions = getRhoPartitions();
        //The last partition is the finest one.
        Partition bestPartition = partitions.get(partitions.size() - 1);

        //Create new state, build mapping from old state to the new one.
        Set<State> stateSet = new HashSet<>();
        for (Block block : bestPartition.getBlocks()) {
            //Any state in the block
            State newState = new State(block.getStates().iterator().next().getSignature());
            stateSet.add(newState);
            for (State oldState : block.getStates()) {
                stateMap.put(oldState, newState);
            }
        }
        //Create transitions
        Set<Transition> tranSet = new HashSet<>();
        //For each old transition, build a new transition whose ends are the mapped new state.
        for (Transition tran : getTransitionSet()) {
            DTransition oldTrans = (DTransition) tran;
            DTransition newTrans = new DTransition(stateMap.get(oldTrans.getSrcState()), oldTrans.getInput(), oldTrans.getOutput(), stateMap.get(oldTrans.getDesState()));
            tranSet.add(newTrans);
        }

        MealyMachine minimizedMachine = new MealyMachine(getInputSet(), getOutputSet(), stateSet, stateMap.get(getInitialState()), tranSet);

        return minimizedMachine;
    }

    public static void main(String[] args) throws IOException, BiffException {
        MealyMachine door = FileUtil.buildMMachineFromExcel("C:\\Users\\87720\\OneDrive\\Coding Workspace\\IdeaProjects\\TestPlatform\\assets\\door.xls");
        Map<State, State> stateMap = new HashMap<>();
        MealyMachine reducedDoor = door.minimize(stateMap);
        System.out.println("OLD MODEL:");
        System.out.println("******************************");
        System.out.println(door.print());
        System.out.println("REDUCED MODEL:");
        System.out.println("******************************");
        System.out.println(reducedDoor.print());
    }
}
