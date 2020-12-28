package hx.mbt.pot;

import hx.mbt.fsm.InputSequence;
import hx.mbt.fsm.OutputSequence;

import java.util.Objects;

public class TestCase {
    private InputSequence inputSequence;
    private OutputSequence expectedOutputSequence;

    public TestCase() {

    }

    public TestCase(InputSequence inputSequence) {
        this.inputSequence = inputSequence;
        this.expectedOutputSequence = new OutputSequence();
    }

    public InputSequence getInputSequence() {
        return inputSequence;
    }

    public void setInputSequence(InputSequence inputSequence) {
        this.inputSequence = inputSequence;
    }

    public OutputSequence getExpectedOutputSequence() {
        return expectedOutputSequence;
    }

    public void setExpectedOutputSequence(OutputSequence expectedOutputSequence) {
        this.expectedOutputSequence = expectedOutputSequence;
    }

    public String print(){
        String text = "";

        text += "Input Sequence: "+ inputSequence.print();

        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCase testCase = (TestCase) o;
        return getInputSequence().equals(testCase.getInputSequence());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInputSequence());
    }
}
