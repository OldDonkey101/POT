package hx.mbt.fsm;

import java.util.*;

public class InputSequence extends Sequence{


    public InputSequence() {
        super(new ArrayList<Entity>());
    }

    public static InputSequence createEmptyInputSeq() {
        return new InputSequence();
    }


    @Override
    public <T extends Sequence> T copy() {
        InputSequence newSeq = createEmptySequence();
        for (Entity e : this.getEntityList()) {
            newSeq.getEntityList().add(e);
        }
        return (T) newSeq;
    }

    @Override
    public <T extends Sequence> T createEmptySequence() {
        return (T) new InputSequence();
    }

}
