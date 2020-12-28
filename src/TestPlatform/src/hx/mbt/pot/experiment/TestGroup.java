package hx.mbt.pot.experiment;

import hx.mbt.fsm.*;
import hx.mbt.pot.FileUtil;
import hx.mbt.pot.TestArgs;
import hx.mbt.pot.TestCost;
import hx.mbt.pot.TestCostTrivialReset;
import jxl.write.WriteException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TestGroup {

    private String name;
//    private Collection<TestExp> exps;
    private TestArgs testArgs;

    public TestGroup(String name, TestArgs testArgs) {
        this.name = name;
        this.testArgs = testArgs;
    }

    public boolean execute() throws IOException, WriteException {
        long starTime = System.currentTimeMillis();
        System.out.println("Begin to execute Test Group: " + getName());
        for (int i = 0; i < testArgs.getTestRuns(); i++) {
            System.out.print("+");
        }
        System.out.println();
        for (int i = 0; i < testArgs.getTestRuns(); i++) {
            HomoMapping mapping = null;
            while (mapping==null ||
                    mapping.getA().getStateSet().size() != testArgs.getStateNumOfA() ||
                    mapping.getS().getStateSet().size()!= testArgs.getStateNumOfS() ) {
                Map<State, State> stateMap = new HashMap<>();
                MealyMachine machine = FSMGenerator.buildRandomMMachine(testArgs.getInputNum(), testArgs.getOutputNum(), testArgs.getStateNumOfS());
                FSM fsm = FSMGenerator.expand(machine, testArgs.getStateNumOfA(), testArgs.getAverTransNumOfA(), stateMap);
                mapping = new HomoMapping(machine, fsm, stateMap);
                mapping.reduce();
            }
            TestExp exp = new TestExp(mapping);
            TestCost testCost = new TestCostTrivialReset();
            exp.execute(testCost,testArgs.getK());
            FileUtil.writeTestExp2Excel(exp, null, this.name);
            System.out.print("=");
        }
        System.out.println();
        long endTime = System.currentTimeMillis();
        long usedTime = endTime-starTime;
        System.out.println("Take time: " + usedTime/1000+"s");
        return true;
    }

    public String getName() {
        return name;
    }

    public static void main(String[] args) throws IOException, WriteException {
        Random random = new Random();
        for (int i = 4; i <= 9; i++) {
            int inputNum = 10;
            int outputNum = 10;
            int stateNumOfS = i;
            int stateNumOfA = 4;
            int aveTrans = 800;
            int k = 0;

            TestArgs testArgs = new TestArgs(inputNum,outputNum,stateNumOfS,stateNumOfA,100,aveTrans,k);
            TestGroup testGroup = new TestGroup(testArgs.createTestName(), testArgs);
            testGroup.execute();
        }
        for (int i = 1; i <= 5; i++) {
            int inputNum = 10;
            int outputNum = 10;
            int stateNumOfS = i*10;
            int stateNumOfA = 4;
            int aveTrans = 800;
            int k = 0;

            TestArgs testArgs = new TestArgs(inputNum,outputNum,stateNumOfS,stateNumOfA,100,aveTrans,k);
            TestGroup testGroup = new TestGroup(testArgs.createTestName(), testArgs);
            testGroup.execute();
        }
    }
}
