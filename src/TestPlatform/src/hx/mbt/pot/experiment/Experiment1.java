package hx.mbt.pot.experiment;

import hx.mbt.fsm.*;
import hx.mbt.pot.*;
import jxl.read.biff.BiffException;

import java.io.IOException;
import java.util.Collection;

public class Experiment1 {
    public static void main(String[] args) throws IOException, BiffException {
        String mappingFilePath = "assets\\mapping-CSM.xls";
        String safeImpsFilePath = "assets\\I-CSM-Safe.xls";
        String unsafeImpsFilePath = "assets\\I-CSM-Unsafe.xls";
        //Create specification model, PoI model and the mapping from the mapping file.
        HomoMapping mapping = FileUtil.buildMappingFromExcel(mappingFilePath);
        mapping.reduce();
        mapping.getS().buildSepSeqSet();

        //Calculate test cost by steps.
        TestCost testCost = new TestCostTrivialReset();

        POT pot =new POT();
        //Generate test suite using P-Method.
        TestSuite TSofPOT =  pot.deriveCompleteTSOfPOT(mapping, 4, testCost, 3);
        TSofPOT = TSofPOT.partial();
        TSofPOT.buildOracle(mapping.getS());
        System.out.println("Number of Test cases of POT:" + TSofPOT.getTestCaseSet().size());

        //Generate test suite using H-Method.
        TestSuite TSofConf = pot.deriveCompleteTSOfConf(mapping.getS(), 4, testCost, 3);
        TSofConf = TSofConf.partial();
        TSofConf.buildOracle(mapping.getS());
        System.out.println("Number of Test cases of Conf:" + TSofConf.getTestCaseSet().size());
        System.out.println();

        //Use test suites generated above to test incorrect yet safe implementations.
        System.out.println("Begin to test(Safe implementations):");
        System.out.println("========================");
        Collection<MealyMachine> safeImpls = FileUtil.buildMMachinesFromExcel(safeImpsFilePath);
        int NumOfImps = 0;
        int failedByPOT = 0;
        int failedByConf = 0;
        for (MealyMachine impl : safeImpls) {
            Tester potTester = new Tester(TSofPOT, impl);
            Tester confTester = new Tester(TSofConf, impl);
            NumOfImps ++;
            if (!potTester.execute()) failedByPOT++;
            if (!confTester.execute()) failedByConf++;
        }
        System.out.println("Number of Implementations: " + NumOfImps);
        System.out.println("Number of Implementations failed by POT: " + failedByPOT);
        System.out.println("Number of Implementations failed by Conf: " + failedByConf);
        System.out.println();

        //Use test suites generated above to test incorrect and unsafe implementations.
        System.out.println("Begin to test(Unsafe implementations):");
        System.out.println("========================");
        Collection<MealyMachine> unsafeImpls = FileUtil.buildMMachinesFromExcel(unsafeImpsFilePath);
        NumOfImps = 0;
        failedByPOT = 0;
        failedByConf = 0;
        for (MealyMachine impl : unsafeImpls) {
            Tester potTester = new Tester(TSofPOT, impl);
            Tester confTester = new Tester(TSofConf, impl);
            NumOfImps ++;
            if (!potTester.execute()) failedByPOT++;
            if (!confTester.execute()) failedByConf++;
        }
        System.out.println("Number of Implementations: " + NumOfImps);
        System.out.println("Number of Implementations failed by POT: " + failedByPOT);
        System.out.println("Number of Implementations failed by Conf: " + failedByConf);
    }
}
