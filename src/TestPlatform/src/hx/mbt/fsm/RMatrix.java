package hx.mbt.fsm;

import java.util.ArrayList;
import java.util.Set;

public class RMatrix {
    private FSM A;
    private int[][] R;
    private int n;
    private ArrayList<State> states;//需要将A的状态顺序化
    private int zeroValueCount;

    public RMatrix(FSM a) {
        A = a;
        n = a.getStateSet().size();
        R = new int[n][n];
        states = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                R[i][j] = 0;
            }
        }
        for (State s : A.getStateSet()) {
            states.add(s);
        }
        buildR();
    }

    public String print(){
        String text = "";
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                text += R[i][j] + "\t";
                if (j == n-1) text += "\n";
            }
        }
        return text;
    }

    public int getN(){
        return n;
    }

    public int[][] buildR(){
        //Step0:

        //Step1: initialization
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    setValue(i, j, 1);
                } else {
                    State si = states.get(i);
                    State sj = states.get(j);
                    setValue(i, j, 0);
                    for (Input x : A.getInputSet()) {
                        Set<Output> OSetI = A.outputs(si, x);
                        Set<Output> OSetJ = A.outputs(sj, x);
                        if (!OSetJ.containsAll(OSetI)) {
                            setValue(i, j, - 1);
                        }
                    }
                }
            }
        }
        //Step2: updating.
        int l1 = 1;
        int l0 = 0;
        while (l1 > l0) {
            l1 = getZeroValueCount();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (get(i, j) == 0) {
                        State si = states.get(i);
                        State sj = states.get(j);
                        boolean traceNotIn = false;
                        for (Input x : A.getInputSet()) {
                            for (Output y : A.outputs(si, x)) {
                                for (State sp : A.transitToWithOutput(si, x, y)) {
                                    int p = getIndex(sp);
                                    boolean flag = false;
                                    for (State sq : A.transitToWithOutput(sj, x, y)) {
                                        int q = getIndex(sq);
                                        if (get(p, q) != -1) {
                                            flag = true;
                                            break;
                                        }
                                    }
                                    if(!flag) {traceNotIn=true;break;}
                                }
                                if(traceNotIn) break;
                            }
                            if(traceNotIn) break;
                        }
                        if(traceNotIn) setValue(i,j,-1);
                    }
                }
            }
            l0 = getZeroValueCount();
        }

        //Set 0 to 1
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (get(i, j) == 0) {
                    setValue(i, j, 1);
                }
            }
        }

        return R;
    }

    private int getZeroValueCount() {
        return zeroValueCount;
    }

    private void setValue(int i, int j, int value) {
        if (R[i][j] != 0 && value == 0) {
            zeroValueCount++;
        } else if (R[i][j] == 0 && value != 0) {
            zeroValueCount--;
        }
        R[i][j] = value;
    }

    public int get(int i, int j) {
        return R[i][j];
    }

    public int get(State s1, State s2) {
        return get(getIndex(s1), getIndex(s2));
    }

    public int getIndex(State state) {
        for (int i = 0; i < n; i++) {
            if (states.get(i).equals(state)) {
                return i;
            }
        }
        return 0;
    }

    public int getNumOf1Entries(){
        int count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (get(i, j) == 1) count++;
            }
        }
        return count;
    }
}
