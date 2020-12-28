package hx.mbt.pot.experiment;

import hx.mbt.fsm.MealyMachine;
import hx.mbt.fsm.OutputSequence;
import hx.mbt.pot.TestCase;
import hx.mbt.pot.TestSuite;

public class Tester {
    private TestSuite TS;
    private MealyMachine impl;

    public Tester(TestSuite TS, MealyMachine impl) {
        this.TS = TS;
        this.impl = impl;
    }

    public boolean execute() {
        return passTestSuite(TS, impl);
    }

    public static boolean passTestCase(TestCase tc, MealyMachine impl) {
        OutputSequence outputSequence = impl.getOutputSeq(impl.getInitialState(), tc.getInputSequence());
        return outputSequence.equals(tc.getExpectedOutputSequence());
    }

    public static boolean passTestSuite(TestSuite ts, MealyMachine impl) {
        for (TestCase tc : ts.getTestCaseSet()) {
            if (!passTestCase(tc, impl)) {
                return false;
            }
        }
        return true;
    }
}
