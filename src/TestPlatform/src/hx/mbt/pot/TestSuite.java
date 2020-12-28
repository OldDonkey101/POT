package hx.mbt.pot;

import hx.mbt.fsm.InputSequence;
import hx.mbt.fsm.MealyMachine;
import hx.mbt.fsm.Sequence;

import java.util.HashSet;
import java.util.Set;

public class TestSuite {
    private Set<TestCase> tcSet;

    public TestSuite() {
        this.tcSet = new HashSet<TestCase>();
    }

    public void addInputSequence(InputSequence sequence) {
        this.tcSet.add(new TestCase(sequence));
    }

    public void addAllInputSequences(Set<InputSequence> sequences) {
        for (InputSequence sequence : sequences) {
            this.addInputSequence(sequence);
        }
    }

    public int costSize(){
        return this.getTestCaseSet().size();
    }
    public int costStep(){
        int cost = 0;
        for (TestCase tc : getTestCaseSet()) {
            cost += tc.getInputSequence().getLength();
        }
        cost += getTestCaseSet().size();
        return cost;
    }

    public Set<TestCase> getTestCaseSet() {
        return tcSet;
    }

    public Set<InputSequence> getInputSeqSet() {
        //TODO: 效率太低
        Set<InputSequence> results = new HashSet<>();
        for (TestCase tc : tcSet) {
            results.add(tc.getInputSequence());
        }
        return results;
    }

    public String print() {
        String text = "";

        for (TestCase tc: tcSet) {
            text += "\r\n";
            text += tc.print();
        }

        return text;
    }

    public void buildOracle(MealyMachine spec){
        for (TestCase tc : getTestCaseSet()) {
            tc.setExpectedOutputSequence(spec.getOutputSeq(spec.getInitialState(), tc.getInputSequence()));
        }
    }

    public TestSuite partial(){
        TestSuite TS = new TestSuite();
        Set<InputSequence> inputSequences = getInputSeqSet();
        inputSequences = Sequence.partialSet(inputSequences);
        TS.addAllInputSequences(inputSequences);
        return TS;
    }
}
