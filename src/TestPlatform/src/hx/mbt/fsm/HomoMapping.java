package hx.mbt.fsm;

import hx.mbt.pot.FileUtil;
import jxl.read.biff.BiffException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HomoMapping {
    private MealyMachine S;
    private FSM A;
    private Map<State, State> map;

    public HomoMapping(MealyMachine s, FSM a) {
        S = s;
        A = a;
        map = new HashMap<>();
    }
    public HomoMapping(MealyMachine s, FSM a, Map<State, State> stateMap) {
        S = s;
        A = a;
        map = stateMap;
    }

    public void reduce(){
        Map<State, State> SReduceMap = new HashMap<>();
        Map<State, State> AReduceMap = new HashMap<>();
        S = S.minimize(SReduceMap);
        A = A.reduce(AReduceMap);
        Map<State, State> newMap = new HashMap<>();
        for (Map.Entry<State, State> entry : map.entrySet()) {
            newMap.put(SReduceMap.get(entry.getKey()), AReduceMap.get(entry.getValue()));
        }
        this.map = newMap;
    }

    public MealyMachine getS() {
        return S;
    }

    public void setS(MealyMachine s) {
        S = s;
    }

    public FSM getA() {
        return A;
    }

    public void setA(FSM a) {
        A = a;
    }

    public void addMapping(State s, State a) {
        this.map.put(s, a);
    }

    public State getAState(State state) {
        return map.get(state);
    }

    public static void main(String[] args) throws IOException, BiffException {
        HomoMapping mapping = FileUtil.buildMappingFromExcel("C:\\\\Users\\\\87720\\\\OneDrive\\\\Coding Workspace\\\\IdeaProjects\\\\TestPlatform\\\\assets\\\\mapping-door.xls");
        mapping.reduce();
        System.out.println("SPEC");
        System.out.println("*********************************");
        System.out.println(mapping.getS().print());
        System.out.println("PROPERTY");
        System.out.println("*********************************");
        System.out.println(mapping.getA().print());

    }
}
