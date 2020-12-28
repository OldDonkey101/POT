package hx.mbt.pot.experiment;

import hx.mbt.fsm.HomoMapping;
import hx.mbt.pot.*;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import java.io.IOException;
import java.util.Random;

public class Experiment2 {

    public static void main(String[] args) throws IOException, WriteException {
        Random random = new Random();
        for (int i = 4; i <= 9; i++) {
            //Modify model arguments to run experiment groups.
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
    }
}
