package hx.mbt.pot.experiment;

import hx.mbt.fsm.HomoMapping;
import hx.mbt.fsm.RMatrix;
import hx.mbt.pot.POT;
import hx.mbt.pot.TestCost;
import hx.mbt.pot.TestSuite;

public class TestExp {
    private HomoMapping mapping;
    private int specN;
    private int propN;
    private int TSizePOT;
    private int TStepPOT;
    private int TSizeConf;
    private int TStepConf;
    private int NumberOf1Entries;
    private int propTransNum;
    private int specTranNum;
    private boolean executed;
    private int K;

    public int getK() {
        return K;
    }

    public HomoMapping getMapping() {
        return mapping;
    }

    public boolean isExecuted() {
        return executed;
    }

    public int getSpecN() {
        return specN;
    }

    public int getPropN() {
        return propN;
    }

    public int getTSizePOT() {
        return TSizePOT;
    }

    public int getTStepPOT() {
        return TStepPOT;
    }

    public int getTSizeConf() {
        return TSizeConf;
    }

    public int getTStepConf() {
        return TStepConf;
    }

    public int getNumberOf1Entries() {
        return NumberOf1Entries;
    }

    public int getPropTransNum() {
        return propTransNum;
    }

    public int getSpecTranNum() {
        return specTranNum;
    }

    public RMatrix getR() {
        return R;
    }

    private RMatrix R;

    public TestExp(HomoMapping mapping) {
        this.mapping = mapping;
        mapping.reduce();
        mapping.getS().buildSepSeqSet();
        this.specN = mapping.getS().getStateSet().size();
        this.propN = mapping.getA().getStateSet().size();
        this.propTransNum = mapping.getA().getTransitionSet().size();
        this.specTranNum = mapping.getS().getTransitionSet().size();
        this.executed = false;
    }

    public long execute(TestCost testCost, int k){
        this.K = k;
        long startTime = System.currentTimeMillis();

        POT pot =new POT();
        int m = k + mapping.getS().getStateSet().size();

        TestSuite TSofPOT =  pot.deriveCompleteTSOfPOT(mapping, m, testCost, 0);
        TSofPOT = TSofPOT.partial();
        TSizePOT = TSofPOT.costSize();
        TStepPOT = TSofPOT.costStep();

        TestSuite TSofConf = pot.deriveCompleteTSOfConf(mapping.getS(), m, testCost,0);
        TSofConf = TSofConf.partial();
        TSizeConf = TSofConf.costSize();
        TStepConf = TSofConf.costStep();


        R = new RMatrix(mapping.getA());
        NumberOf1Entries = R.getNumOf1Entries() - R.getN();
        this.executed = true;

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    public String getInfo(){
        if(!executed) return "Test experiment is not executed yet!";

        String text = "";
        text += "Number of states of specification: " + specN + "\n";
        text += "Number of states of property: " + propN + "\n";
        text += "Number of transitions of spec: " + specTranNum + "\n";
        text += "Number of transitions of property: " + propTransNum + "\n";
        text += "Size of POT test suite: " + TSizePOT + "\n";
        text += "Size of Conf test suite: " + TSizeConf + "\n";
        text += "Steps of POT test suite: " + TStepPOT + "\n";
        text += "Steps of Conf test suite: " + TStepConf + "\n";
        text += "Extra 1-valued entries in R:" + NumberOf1Entries + "\n";
//        text += "R Matrix of property:\n";
//        text += R.print();

        return text;

    }
}
