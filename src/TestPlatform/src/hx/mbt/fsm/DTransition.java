package hx.mbt.fsm;

import java.util.HashSet;
import java.util.Set;

public class DTransition extends Transition{
    public DTransition(State srcState, Input input, Output output, State desState) {
        super(srcState,
                new HashSet<Input>(){{add(input);}},
                new HashSet<Output>(){{add(output);}},
                new HashSet<State>(){{add(desState);}});
    }
    public State getDesState(){
        return getDesStates().iterator().next();
    }
    public Input getInput(){
        return getInputs().iterator().next();
    }
    public Output getOutput(){
        return getOutputs().iterator().next();
    }

    @Override
    public Transition copy() {
        return new DTransition(getSrcState(), getInput(), getOutput(), getDesState());
    }
}
