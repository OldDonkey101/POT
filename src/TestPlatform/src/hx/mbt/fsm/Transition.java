package hx.mbt.fsm;

import sun.util.resources.cldr.zh.CalendarData_zh_Hans_HK;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Transition {
    private State srcState;
    private EntitySet<Input> inputs;
    private EntitySet<Output> outputs;
    private EntitySet<State> desStates;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transition that = (Transition) o;
        return getSrcState().equals(that.getSrcState())
                && getInputSet().equals(that.getInputSet())
                && getOutputSet().equals(that.getOutputSet())
                && getDesStateSet().equals(that.getDesStateSet());
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(getSrcState(), getInputSet(), getOutputSet(), getDesStateSet());
//        System.out.println(getSrcState().print() + ": hashcode="+getSrcState().hashCode());
//        System.out.println(getInputSet().print() + ": hashcode="+getInputSet().hashCode());
//        System.out.println(getOutputSet().print() + ": hashcode="+getOutputSet().hashCode());
//        System.out.println(getDesStateSet().print() + ": hashcode="+getDesStateSet().hashCode());
//        System.out.println(print() + ": hashcode="+hash);
        return hash;
    }

    public Transition(State srcState, Set<Input> inputs, Set<Output> outputs, Set<State> desStates) {
        this.srcState = srcState;
        this.inputs = new EntitySet<>(inputs);
        this.outputs = new EntitySet<>(outputs);
        this.desStates = new EntitySet<>(desStates);
    }

    public Transition copy() {
        Set<Input> inputSet = new HashSet<>(getInputs());
        Set<Output> outputSet = new HashSet<>(getOutputs());
        Set<State> desStateSet = new HashSet<>(getDesStates());
        return new Transition(getSrcState(), inputSet, outputSet, desStateSet);
    }

    public String print(){
        return getSrcState().print() + "->" + getInputSet().print() +"/"+ getOutputSet().print() + "->" +getDesStateSet().print();
    }

    public State getSrcState() {
        return srcState;
    }

    public void setSrcState(State srcState) {
        this.srcState = srcState;
    }

    public Set<Input> getInputs() {
        return inputs.getEntitySet();
    }
    public EntitySet<Input> getInputSet(){
        return inputs;
    }

    public void setInputs(Set<Input> inputs) {
        this.inputs = new EntitySet<>(inputs);
    }

    public Set<Output> getOutputs() {
        return outputs.getEntitySet();
    }
    public EntitySet<Output> getOutputSet(){
        return outputs;
    }

    public void setOutputs(Set<Output> outputs) {
        this.outputs = new EntitySet<>(outputs);
    }

    public Set<State> getDesStates() {
        return desStates.getEntitySet();
    }
    public EntitySet<State> getDesStateSet(){
        return desStates;
    }

    public void setDesStates(Set<State> desStates) {
        this.desStates = new EntitySet<>(desStates);
    }

    public boolean startFrom(State s){
        return this.srcState.equals(s);
    }
    public boolean acceptInput(Input i){
        return this.inputs.getEntitySet().contains(i);
    }
    public boolean hasOutput(Output o){
        return this.outputs.getEntitySet().contains(o);
    }

    public boolean transitTo(State srcState) {
        return this.desStates.getEntitySet().contains(srcState);
    }


}
