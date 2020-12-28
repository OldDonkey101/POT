package hx.mbt.fsm;

import java.util.*;

public class VisitTree {

    private State state;
    private Input input;//jump to this state after receiving input.
    private Output output;//responds to input.
    private Set<VisitTree> children;

    public VisitTree(State state, Input input, Output output) {
        this.state = state;
        this.input = input;
        this.output = output;
        this.children = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VisitTree visitTree = (VisitTree) o;
        return state.equals(visitTree.state) && Objects.equals(input, visitTree.input) && Objects.equals(output, visitTree.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, input, output);
    }

    public State getState() {
        return state;
    }

    public Input getInput() {
        return input;
    }

    public Output getOutput() {
        return output;
    }

    public Set<VisitTree> getChildren() {
        return children;
    }

    public void addChild(VisitTree visitTree) {
        this.children.add(visitTree);
    }

    public void addChild(State s, Input x, Output y) {
        this.children.add(new VisitTree(s,x,y));
    }

    public void addTransition(Transition t) {
        for (State s : t.getDesStates()) {
            if (t.getSrcState().equals(this.getState())) {
                for (Input x : t.getInputs()) {
                    for (Output y : t.getOutputs()) {
                        for (State desState : t.getDesStates()) {
                            this.addChild(desState, x, y);
                        }
                    }
                }
            }
        }
    }

    public void addTransitions(Collection<Transition> transitions) {
        for (Transition t : transitions) {
            addTransition(t);
        }
    }

    public VisitTree copy(){
        return new VisitTree(this.getState(), this.getInput(), this.getOutput());
    }

    public Collection<VisitTree> getLeafNodes(){
        Collection<VisitTree> leafNodeSet = new ArrayList<>();
        Stack<VisitTree> stack = new Stack<>();
        stack.push(this);

        while (!stack.isEmpty()) {
            VisitTree node = stack.pop();
            if (node.children.size() == 0) {
                leafNodeSet.add(node);
            } else {
                for (VisitTree vt : node.children) {
                    stack.push(vt);
                }
            }
        }
        return leafNodeSet;
    }

    public boolean isLeaf() {
        return this.children.size() == 0;
    }

    public Set<State> getReachableState(){
        Set<State> reachableStateSet = new HashSet<State>();

        for (VisitTree vt : getLeafNodes()) {
            reachableStateSet.add(vt.getState());
        }

        return reachableStateSet;
    }

    private VisitTree buildPath(LinkedList<VisitTree> vtList) {
        VisitTree resultVT = vtList.get(0).copy();
        VisitTree leaf = resultVT;
        boolean isFirstVT = true;
        for (VisitTree vt : vtList) {
            if (isFirstVT) {
                isFirstVT = false;
            } else {
                VisitTree newLeaf = vt.copy();
                leaf.addChild(newLeaf);
                leaf = newLeaf;
            }
        }
        return resultVT;
    }

    public Collection<VisitTree> getAllPath(){
        Collection<VisitTree> allPath = new HashSet<>();

        Stack<VisitTree> deepFirst = new Stack<>();
        LinkedList<VisitTree> recorder = new LinkedList<>();

        deepFirst.push(this);
        while (!deepFirst.isEmpty()) {
            VisitTree topVT = deepFirst.pop();
            VisitTree tmpVT = topVT.copy();
            recorder.push(tmpVT);

            if (!topVT.isLeaf()) {
                for (VisitTree child : topVT.children) {
                    deepFirst.push(child);
                }
            } else {
                allPath.add(buildPath(recorder));
                recorder.pop();
            }
        }

        return allPath;

//        Collection<Queue<VisitTree>> qSet = new ArrayList<>();
//        Queue<VisitTree> q = new LinkedList<>();
//        q.offer(this);
//        qSet.add(q);
//
//        boolean allLeafNodes = false;
//        while (!allLeafNodes) {
//            allLeafNodes = true;
//            Collection<Queue<VisitTree>> qSetNew = new ArrayList<>();
//            for (Queue<VisitTree> queue : qSet) {
//                if (queue.peek().children.size() == 0) {
//                    //Leaf Node: end of path
//                    qSetNew.add(queue);
//                } else {
//                    allLeafNodes = false;
//                    for (VisitTree vt : queue.peek().children) {
//                        Queue<VisitTree> qNew = (Queue<VisitTree>) ((LinkedList<VisitTree>) queue).clone();
//                        qNew.add(vt);
//                        qSetNew.add(qNew);
//                    }
//                }
//            }
//            qSet =qSetNew;
//        }
//
//        Collection<VisitTree> vtCollection = new ArrayList<>();
//        for (Queue<VisitTree> qvt : qSet) {
//            VisitTree vtNew = new VisitTree(this.state, null, null);
//            VisitTree tail = vtNew;
//            while (qvt.peek() != null) {
//                VisitTree nextVt = qvt.peek().copy();
//                tail.addChild(nextVt);
//                tail = nextVt;
//            }
//            vtCollection.add(vtNew);
//        }
//        return vtCollection;
    }

    public static OutputSequence getOutputSequenceFromPath(VisitTree vt){
        OutputSequence sequence = new OutputSequence();
        while (vt.children.size() > 0) {
            sequence.concatenateBy(vt.getOutput());
            vt = vt.children.iterator().next();
        }
        return sequence;
    }

    public Set<OutputSequence> getOutputSequences(){
        Set<OutputSequence> sequenceSet = new HashSet<>();
        for (VisitTree vt : getAllPath()) {
            sequenceSet.add(getOutputSequenceFromPath(vt));
        }
        return sequenceSet;
    }



    //    private State state;
//    private Set<Transition> transitionSet;
//    private Set<VisitTree> children;
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        VisitTree visitTree = (VisitTree) o;
//        return getState().equals(visitTree.getState());
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(getState());
//    }
//
//    public VisitTree(State state) {
//        this.state = state;
//        this.transitionSet = new HashSet<Transition>();
//        this.children = new HashSet<VisitTree>();
//    }
//
//    public VisitTree(State state, Set<Transition> transitionSet) {
//        this.state = state;
//        this.transitionSet = transitionSet;
//        for (Transition t : transitionSet) {
//            for (State s : t.getDesStates()) {
//                this.children.add(new VisitTree(s));
//            }
//        }
//    }
//
//    public State getState() {
//        return state;
//    }
//
//    public void setState(State state) {
//        this.state = state;
//    }
//
//    public void addTransition(Transition t) {
//        this.transitionSet.add(t);
//        for (State s : t.getDesStates()) {
//            this.children.add(new VisitTree(s));
//        }
//    }
//
//    public void addTransitions(Collection<Transition> transitions) {
//        for (Transition t : transitions) {
//            addTransition(t);
//        }
//    }
//
//    public Set<VisitTree> getLeafNodes(){
//        Set<VisitTree> leafNodeSet = new HashSet<VisitTree>();
//        Stack<VisitTree> stack = new Stack<VisitTree>();
//        stack.push(this);
//        while (!stack.isEmpty()) {
//            VisitTree node = stack.pop();
//            if (node.transitionSet.size() == 0) {
//                leafNodeSet.add(node);
//            } else {
//                for (VisitTree vt : this.children) {
//                    stack.push(vt);
//                }
//            }
//        }
//        return leafNodeSet;
//    }
//
//    public Set<State> getReachableState(){
//        Set<State> reachableStateSet = new HashSet<State>();
//        for (VisitTree vt : getLeafNodes()) {
//            reachableStateSet.add(vt.getState());
//        }
//
//        return reachableStateSet;
//    }
//
//    public Set<OutputSequence> getOutputSequences(){
//
//        //计算每条状态序列
//        Set<Queue<VisitTree>> qSet = new HashSet<>();
//        Queue<VisitTree> q = new LinkedList<VisitTree>();
//        q.offer(this);
//        qSet.add(q);
//
//        boolean allLeafNodes = false;
//        while (!allLeafNodes) {
//            allLeafNodes = true;
//            Set<Queue<VisitTree>> qSetNew = new HashSet<>();
//            for (Queue<VisitTree> queue : qSet) {
//                if (queue.peek().children.size() == 0) {
//                    //Leaf Node: end of path
//                    qSetNew.add(queue);
//                } else {
//                    allLeafNodes = false;
//                    for (VisitTree vt : queue.peek().children) {
//                        Queue<VisitTree> qNew = (Queue<VisitTree>) ((LinkedList<VisitTree>) queue).clone();
//                        qNew.add(vt);
//                        qSetNew.add(qNew);
//                    }
//                }
//            }
//            qSet =qSetNew;
//        }
//
//        for (Queue<VisitTree> queue : qSet) {
//            Set<OutputSequence> outputSequenceSet = new HashSet<>();
//            while (queue.peek() != null) {
//
//            }
//        }
//    }
//

}
