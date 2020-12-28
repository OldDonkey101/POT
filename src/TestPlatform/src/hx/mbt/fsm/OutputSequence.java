package hx.mbt.fsm;

import java.util.ArrayList;
import java.util.List;

public class OutputSequence extends Sequence{

    public OutputSequence() {
        super(new ArrayList<Entity>());
    }

    @Override
    public <T extends Sequence> T copy() {
        OutputSequence newSeq = createEmptySequence();
        for (Entity e : this.getEntityList()) {
            newSeq.getEntityList().add(e);
        }
        return (T) newSeq;
    }

    @Override
    public <T extends Sequence> T createEmptySequence() {
        return (T) new OutputSequence();
    }
}
