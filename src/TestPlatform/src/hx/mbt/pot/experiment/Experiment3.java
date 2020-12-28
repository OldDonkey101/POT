package hx.mbt.pot.experiment;

import hx.mbt.fsm.HomoMapping;
import hx.mbt.pot.*;
import jxl.read.biff.BiffException;

import java.io.IOException;

public class Experiment3 {
    public static void main(String[] args) throws IOException, BiffException {
        String mappingFilePath_example1 = "assets\\mapping-seatbelt.xls";
        String mappingFilePath_example2 = "assets\\mapping-synthetic.xls";
        HomoMapping mapping;
        POT pot =new POT();
        TestSuite TSofPOT;
        TestSuite TSofConf;
        //Calculate test cost by steps.
        TestCost testCost = new TestCostHeavyReset();


        // Case Study 4.1
        //Create specification model, PoI model and the mapping from the mapping file.
        mapping = FileUtil.buildMappingFromExcel(mappingFilePath_example1);
        mapping.reduce();
        mapping.getS().buildSepSeqSet();
        System.out.println("Case study 4.1:");

        //Generate test suite using P-Method.
        TSofPOT =  pot.deriveCompleteTSOfPOT(mapping, 24, testCost, 3);
        TSofPOT = TSofPOT.partial();
        TSofPOT.buildOracle(mapping.getS());
        System.out.println("Number of Test cases of POT:" + TSofPOT.getTestCaseSet().size());

        //Generate test suite using H-Method.
//        TSofConf = pot.deriveCompleteTSOfConf(mapping.getS(), 24, testCost, 3);
//        TSofConf = TSofConf.partial();
//        TSofConf.buildOracle(mapping.getS());
//        System.out.println("Number of Test cases of Conf:" + TSofConf.getTestCaseSet().size());
        System.out.println("==========================================================");

        // Case Study 4.2
        mapping = FileUtil.buildMappingFromExcel(mappingFilePath_example2);
        mapping.reduce();
        mapping.getS().buildSepSeqSet();
        System.out.println("Case study 4.2:");
        //Generate test suite using P-Method.
        TSofPOT =  pot.deriveCompleteTSOfPOT(mapping, 7, testCost, 3);
        TSofPOT = TSofPOT.partial();
        TSofPOT.buildOracle(mapping.getS());
        System.out.println("Number of Test cases of POT:" + TSofPOT.getTestCaseSet().size());

        //Generate test suite using H-Method.
        TSofConf = pot.deriveCompleteTSOfConf(mapping.getS(), 7, testCost, 3);
        TSofConf = TSofConf.partial();
        TSofConf.buildOracle(mapping.getS());
        System.out.println("Number of Test cases of Conf:" + TSofConf.getTestCaseSet().size());
        System.out.println("==========================================================");
    }
}
