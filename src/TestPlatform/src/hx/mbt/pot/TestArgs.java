package hx.mbt.pot;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TestArgs{
    private int inputNum;
    private int outputNum;
    private int stateNumOfS;
    private int stateNumOfA;
    private int testRuns;
//    private int prop1;
//    private int prop2;
    private int k;
    private int averTransNumOfA;

    public TestArgs(int inputNum, int outputNum, int stateNumOfS, int stateNumOfA, int testRuns, int averTransNumOfA, int k) {
        this.inputNum = inputNum;
        this.outputNum = outputNum;
        this.stateNumOfS = stateNumOfS;
        this.stateNumOfA = stateNumOfA;
        this.testRuns = testRuns;
//        this.prop1 = prop1;
//        this.prop2 = prop2;
        this.averTransNumOfA = averTransNumOfA;
        this.k = k;
    }

    public String createTestName(){
        ArrayList<String> names = new ArrayList<>();
        names.add(inputNum + "");
        names.add(outputNum + "");
        names.add(stateNumOfS + "");
        names.add(stateNumOfA + "");
//        names.add(prop1 + "");
//        names.add(prop2 + "");
        names.add(averTransNumOfA + "");
        names.add(k + "");

        return String.join("X", names);
    }

    public int getK() {
        return k;
    }

    public int getInputNum() {
        return inputNum;
    }

    public int getOutputNum() {
        return outputNum;
    }

    public int getStateNumOfS() {
        return stateNumOfS;
    }

    public int getStateNumOfA() {
        return stateNumOfA;
    }

    public int getTestRuns() {
        return testRuns;
    }

//    public int getProp1() {
//        return prop1;
//    }
//
//    public int getProp2() {
//        return prop2;
//    }

    public int getAverTransNumOfA() {
        return averTransNumOfA;
    }
}
