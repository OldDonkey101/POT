package hx.mbt.fsm;

import java.util.HashSet;
import java.util.Set;

public class Input extends Entity{

    public Input(String signature) {
        super(signature);
    }

//    public static Set<Entity> toEntitySet(Set<Input> inputSet) {
//        Set<Entity> entitySet = new HashSet<Entity>();
//        for (Input input : inputSet) {
//            entitySet.add(input);
//        }
//        return entitySet;
//    }
}
