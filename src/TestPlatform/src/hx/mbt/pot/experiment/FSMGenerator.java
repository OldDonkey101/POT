package hx.mbt.pot.experiment;

import hx.mbt.fsm.*;
import hx.mbt.pot.TestCost;
import hx.mbt.pot.TestCostTrivialReset;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import java.io.IOException;
import java.util.*;

public class FSMGenerator {
    public static MealyMachine buildRandomMMachine(int inputNum, int outputNum, int stateNum) {
        ArrayList<Input> inputSet = new ArrayList<>();
        ArrayList<Output> outputSet = new ArrayList<>();
        ArrayList<State> stateSet = new ArrayList<>();

        for (int i = 0; i < inputNum; i++) {
            inputSet.add(new Input("i" + i));
        }
        for (int i = 0; i < outputNum; i++) {
            outputSet.add(new Output("o" + i));
        }
        for (int i = 0; i < stateNum; i++) {
            stateSet.add(new State("s" + i));
        }

        //Generate transitions randomly.
        Random random = new Random();
        Set<Transition> transitionSet = new HashSet<>();
        for (int i = 0; i < stateNum; i++) {
            for (int j = 0; j < inputNum; j++) {
                transitionSet.add(new DTransition(
                        stateSet.get(i),
                        inputSet.get(j),
                        outputSet.get(random.nextInt(outputNum)),
                        stateSet.get(random.nextInt(stateNum))));
            }
        }

        MealyMachine machine =  new MealyMachine(
                new HashSet<>(inputSet),
                new HashSet<>(outputSet),
                new HashSet<>(stateSet),
                stateSet.get(0),
                transitionSet
        );
        if (machine.isConnected()) {
            return machine;
        } else {
            return buildRandomMMachine(inputNum, outputNum, stateNum);
        }
    }

//    public static Set<MealyMachine> buildRandomConnectedMMachines(int inputNum, int outputNum, int stateNum, int machineNum) {
//        Set<MealyMachine> machineSet = new HashSet<>();
//
//        while (machineSet.size() < machineNum) {
//            MealyMachine newMachine = buildRandomMMachine(inputNum, outputNum, stateNum);
//            if (newMachine.isConnected()) machineSet.add(newMachine);
//        }
//
//        return machineSet;
//    }

    public static FSM expandByTransNumOfA(FSM machine, int transNumOfA, Map<State,State> stateMap) {
//        int extraTransNum = transNumOfA - machine.getTransitionSet().size();
        //fixed that prop1 = 100, calculate pro2.
//        int pro2 = (int) ((transNumOfA-machine.getTransitionSet().size()) *100.0 / ((machine.getOutputSet().size()*machine.getStateSet().size()-1)*machine.getTransitionSet().size()));
//        return expandByProp(machine, 100, pro2, stateMap);
        FSM fsm = machine.copy();
        Random random = new Random();
        ArrayList<State> stateList = new ArrayList<>(machine.getStateSet());
        ArrayList<Input> inputList = new ArrayList<>(machine.getInputSet());
        ArrayList<Output> outputList = new ArrayList<>(machine.getOutputSet());

        while (fsm.getTransitionSet().size() < transNumOfA) {
            // add a new random transition
            int srcStateIndex = random.nextInt(stateList.size());
            int desStateIndex = random.nextInt(stateList.size());
            int inputIndex = random.nextInt(inputList.size());
            int outputIndex = random.nextInt(outputList.size());
            fsm.addTransition(new Transition(
                    stateList.get(srcStateIndex),
                    new HashSet<Input>(){{add(inputList.get(inputIndex));}},
                    new HashSet<Output>(){{add(outputList.get(outputIndex));}},
                    new HashSet<State>(){{add(stateList.get(desStateIndex));}}
            ));
        }
        for (State state : machine.getStateSet()) {
            stateMap.put(state, state);
        }
        return fsm;
    }

    public static FSM expandByProp(FSM machine, int probability, int probNewTrans, Map<State,State> stateMap) {
        assert (0<=probability && probability<=100);
        FSM fsm = machine.copy();
        Random random = new Random();
        for (Transition transition : machine.getTransitionSet()) {
            if (random.nextInt(100)+1 <= probability) {
                for (Output output : fsm.getOutputSet()) {
                    for (State desState : fsm.getStateSet()) {
                        if (random.nextInt(100)+1 <= probNewTrans) fsm.addTransition(
                                new Transition(
                                        transition.getSrcState(),
                                        transition.getInputs(),
                                        new HashSet<Output>(){{add(output);}},
                                        new HashSet<State>(){{add(desState);}}));
                    }
                }
            }
        }
        for (State state : machine.getStateSet()) {
            stateMap.put(state, state);
        }
        return fsm;
    }
    public static FSM expandByFixedSize(FSM machine, int m, Map<State, State> stateMap) {
        FSM fsm = machine.copy();
        Random random = new Random();
        //Partition states of machine into m blocks randomly.
        Partition partition = new Partition();
//        ArrayList<Block> blocks = new ArrayList<>();
        Block[] blocks = new Block[m];
        ArrayList<State> stateList = new ArrayList<>(machine.getStateSet());
        Collections.shuffle(stateList);
        for (int i = 0; i < m; i++) {
            blocks[i] = new Block();
            blocks[i].addState(stateList.get(i));
        }
        for (int i = m; i< stateList.size();i++) {
            int randomIndex = random.nextInt(m);
            blocks[randomIndex].addState(stateList.get(i));
        }
        partition.addBlocks(Arrays.asList(blocks));

        //Build new fsm based on partition
        return fsm.buildNewFSMBasedOnPartition(partition, stateMap);
    }

    public static FSM expand(FSM originalMachine, int maxSize, int transNumOfA, Map<State, State> stateMap) {
        FSM firstExpand = expandByFixedSize(originalMachine, maxSize, stateMap);
        return expandByTransNumOfA(firstExpand,transNumOfA, new HashMap<State, State>());//stateMap means nothing here.
    }
    public static FSM expand(FSM originalMachine, int maxSize, int pro1, int pro2, Map<State, State> stateMap) {
        FSM firstExpand = expandByFixedSize(originalMachine, maxSize, stateMap);
        return expandByProp(firstExpand,pro1, pro2, new HashMap<State, State>());//stateMap means nothing here.
    }


    public static <T extends Entity> Set<T> getRandomSubSet(Set<T> originalSet) {
        Random random = new Random();
        Set<T> resultSet = new HashSet<>();
        for (T entity : originalSet) {
            if(random.nextBoolean()) resultSet.add(entity);
        }
        return resultSet;
    }

    public static void main(String[] args) throws IOException, BiffException, WriteException {
        Map<State, State> stateMap = new HashMap<>();
        MealyMachine machine = FSMGenerator.buildRandomMMachine(4, 4, 40);
//        FSM fsm = FSMGenerator.expandByFixedSize(machine, 4, stateMap);
//        FSM fsm = FSMGenerator.buildFSMBasedOnMM_Prop(machine, 100, 60, stateMap);
        FSM fsm = FSMGenerator.expand(machine, 4, 50,stateMap);
        HomoMapping mapping = new HomoMapping(machine, fsm, stateMap);

//        HomoMapping mapping = Util.buildMappingFromExcel("C:\\\\Users\\\\87720\\\\OneDrive\\\\Coding Workspace\\\\IdeaProjects\\\\TestPlatform\\\\assets\\\\mapping-synthetic.xls");
        TestExp exp = new TestExp(mapping);
        TestCost testCost = new TestCostTrivialReset();
        long usedTime = exp.execute(testCost,0)/1000;
        System.out.println("Used Time:" + usedTime+"s");
        System.out.println(exp.getInfo());
//        Util.writeTestExp2Excel(exp, null, );

    }
}
