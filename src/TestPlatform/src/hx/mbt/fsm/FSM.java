package hx.mbt.fsm;

import hx.mbt.pot.FileUtil;
import jxl.read.biff.BiffException;

import java.io.IOException;
import java.util.*;

public class FSM {

    private EntitySet<Input> inputSet;
    private EntitySet<Output> outputSet;
    private EntitySet<State> stateSet;
    private State initialState;
    private Set<Transition> transitionSet;

    private State currentState;

    public FSM(Set<Input> inputSet, Set<Output> outputSet, Set<State> stateSet, State initialState, Set<Transition> transitionSet) {
        this.inputSet = new EntitySet<>(inputSet);
        this.outputSet = new EntitySet<>(outputSet);
        this.stateSet = new EntitySet<>(stateSet);
        this.initialState = initialState;
        this.transitionSet = transitionSet;

        this.currentState = initialState;
    }

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
        return new FSM(inputSet, outputSet, stateSet, new State(getInitialState().getSignature()), transitionSet);
    }

    public String print(){
        String text = "";
        for (Transition transition : getTransitionSet()) {
            text += "\r\n";
            text += transition.print();
        }
        return text;
    }

    public void addTransition(Transition transition) {
        this.transitionSet.add(transition);
    }

    public void resetCurrentState(){
        this.currentState = initialState;
    }
    public State getCurrentState(){
        return this.currentState;
    }

    public Set<Input> getInputSet() {
        return inputSet.getEntitySet();
    }

    public void setInputSet(Set<Input> inputSet) {
        this.inputSet = new EntitySet<>(inputSet);
    }

    public Set<Output> getOutputSet() {
        return outputSet.getEntitySet();
    }

    public void setOutputSet(Set<Output> outputSet) {
        this.outputSet = new EntitySet<>(outputSet);
    }

    public Set<State> getStateSet() {
        return stateSet.getEntitySet();
    }

    public void setStateSet(Set<State> stateSet) {
        this.stateSet = new EntitySet<>(stateSet);
    }

    public State getInitialState() {
        return initialState;
    }

    public void setInitialState(State initialState) {
        this.initialState = initialState;
    }

    public Set<Transition> getTransitionSet() {
        return transitionSet;
    }

    public void setTransitionSet(Set<Transition> transitionSet) {
        this.transitionSet = transitionSet;
    }

    public void clearVisitStatus(){
        for (State s : this.getStateSet()) {
            s.clearVisitState();
        }
    }

    public Set<Transition> getTransitionsStartFrom(State srcState) {
        Set<Transition> transitions = new HashSet<Transition>();
        for (Transition trans : getTransitionSet()) {
            if (trans.startFrom(srcState)) {
                transitions.add(trans);
            }
        }
        return transitions;
    }
    public Set<Transition> getTransitionsStartFromWithInput(State srcState, Input input) {
        Set<Transition> transitions = new HashSet<Transition>();
        for (Transition trans : getTransitionSet()) {
            if (trans.startFrom(srcState) && trans.acceptInput(input)) {
                transitions.add(trans);
            }
        }
        return transitions;
    }
    public Set<Transition> getTransitionsToOtherState(State srcState) {
        Set<Transition> transitions = new HashSet<Transition>();
        for (Transition trans : getTransitionSet()) {
            if (trans.startFrom(srcState) && !trans.transitTo(srcState)) {
                transitions.add(trans);
            }
        }
        return transitions;
    }

    public Set<Output> outputs(State currentState, Input input){
        Set<Output> outputs = new HashSet<Output>();
        for (Transition trans : getTransitionsStartFrom(currentState)) {
            if (trans.acceptInput(input)) {
                outputs.addAll(trans.getOutputs());
            }
        }
        return outputs;
    }
    public Set<State> transitTo(State currentState, Input input){
        Set<State> desStates = new HashSet<>();
        for (Transition trans : getTransitionsStartFrom(currentState)) {
            if (trans.getInputs().contains(input)) {
                desStates.addAll(trans.getDesStates());
            }
        }
        return desStates;
    }

    public Set<State> transitToWithOutput(State currentState, Input input, Output output) {
        Set<State> desStates = new HashSet<State>();
        for (Transition trans : getTransitionsStartFrom(currentState)) {
            if (trans.acceptInput(input) && trans.hasOutput(output)) {
                desStates.addAll(trans.getDesStates());
            }
        }
        return desStates;
    }

    public VisitTree getVisitTree(State currentState, InputSequence inputSequence) {
        VisitTree visitTree = new VisitTree(currentState,null,null);

        for (Entity input : inputSequence.getEntityList()) {
            for (VisitTree vt : visitTree.getLeafNodes()) {
                for (Transition t: getTransitionsStartFromWithInput(vt.getState(),(Input) input)){
                    for (Output output : t.getOutputs()) {
                        for (State desState : t.getDesStates()) {
                            vt.addChild(desState, (Input) input, output);
                        }
                    }
                }

            }
        }
        return visitTree;
    }

    public FSM reduce(Map<State,State> stateIDMap){
        RMatrix R = new RMatrix(this);
        //Partition states according to R
        Partition partition = new Partition();
        Block firstBlock = new Block();
        firstBlock.addState(getInitialState());
        partition.addBlock(firstBlock);
        for (State s1 : getStateSet()) {
            boolean needNewBlock = true;
            for (Block block : partition.getBlocks()) {
                for (State s2 : block.getStates()) {
                    if (R.get(s1, s2) == 1 && R.get(s2, s1) == 1) {// s1 = s2
                        block.addState(s1);
                        needNewBlock = false;
                        break;
                    }
                }
            }
            if (needNewBlock) {
                Block newBlock = new Block();
                newBlock.addState(s1);
                partition.addBlock(newBlock);
            }
        }
        //Create New FSM according to partition.
        //Create new state, build mapping from old state to the new one.
        Set<State> stateSet = new HashSet<>();
        for (Block block : partition.getBlocks()) {
            //Any state in the block
            State newState = new State(block.getStates().iterator().next().getSignature());
            stateSet.add(newState);
            for (State oldState : block.getStates()) {
                stateIDMap.put(oldState, newState);
            }
        }
        //Create transitions
        Set<Transition> tranSet = new HashSet<>();
        //For each old transition, build a new transition whose ends are the mapped new state.
        for (Transition oldTrans : getTransitionSet()) {
            Set<State> desStateSet = new HashSet<>();
            for (State oldDesState : oldTrans.getDesStates()) {
                desStateSet.add(stateIDMap.get(oldDesState));
            }
            Transition newTrans = new Transition(
                    stateIDMap.get(oldTrans.getSrcState()),
                    oldTrans.getInputs(),
                    oldTrans.getOutputs(),
                    desStateSet);
            tranSet.add(newTrans);
        }

        FSM minimizedMachine = new FSM(getInputSet(),getOutputSet(),stateSet, stateIDMap.get(getInitialState()),tranSet);

        return minimizedMachine;
    }

    public boolean hasTransitFromTo(State srcState, State desState) {
        for (Transition transition : getTransitionSet()) {
            if (transition.startFrom(srcState) && transition.transitTo(desState)) return true;
        }
        return false;
    }

    public boolean isConnected(){
        Set<State> statesReachableFromInit = new HashSet<>();
        statesReachableFromInit.add(getInitialState());

        while (true) {
            int sizeBefore = statesReachableFromInit.size();
            for (State state : getStateSet()) {
                if (!statesReachableFromInit.contains(state)) {
                    for (State reachableState : statesReachableFromInit) {
                        if (hasTransitFromTo(reachableState, state)) {
                            statesReachableFromInit.add(state);
                            break;
                        }
                    }
                }
            }
            int sizeAfter = statesReachableFromInit.size();
            if (sizeAfter == sizeBefore) break;
        }

        return statesReachableFromInit.size() == getStateSet().size();
    }

    public FSM buildNewFSMBasedOnPartition(Partition partition, Map<State, State> stateMap) {
        //Create New FSM according to partition.
        //Create new state, build mapping from old state to the new one.
        Set<State> stateSet = new HashSet<>();
        for (Block block : partition.getBlocks()) {
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
        for (Transition oldTrans : getTransitionSet()) {
            Set<State> desStateSet = new HashSet<>();
            for (State oldDesState : oldTrans.getDesStates()) {
                desStateSet.add(stateMap.get(oldDesState));
            }
            Transition newTrans = new Transition(
                    stateMap.get(oldTrans.getSrcState()),
                    oldTrans.getInputs(),
                    oldTrans.getOutputs(),
                    desStateSet);
            tranSet.add(newTrans);
        }

        return new FSM(getInputSet(),getOutputSet(),stateSet, stateMap.get(getInitialState()),tranSet);
    }

    public static void main(String[] args) throws IOException, BiffException {
        FSM fsm = FileUtil.buildMappingFromExcel("C:\\\\Users\\\\87720\\\\OneDrive\\\\Coding Workspace\\\\IdeaProjects\\\\TestPlatform\\\\assets\\\\mapping-seatbelt.xls").getA();
        Map<State, State> stateMap = new HashMap<>();
        fsm = fsm.reduce(stateMap);
        System.out.println(fsm.isConnected());
    }

}
