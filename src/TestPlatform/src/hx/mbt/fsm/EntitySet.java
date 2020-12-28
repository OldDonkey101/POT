package hx.mbt.fsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class EntitySet <T extends  Entity>{
    Set<T> entitySet;

    public EntitySet(Set<T> entitySet) {
        this.entitySet = entitySet;
    }

    public Set<T> getEntitySet() {
        return entitySet;
    }
    public int getSize(){
        return this.entitySet.size();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntitySet entitySet1 = (EntitySet) o;
        return getEntitySet().containsAll(entitySet1.getEntitySet()) && entitySet1.getEntitySet().containsAll(getEntitySet());
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (Entity entity : getEntitySet()) {
            hash += entity.hashCode();
        }
        return hash;
    }

    public String print(){
        Collection<String> sigs = new ArrayList<>();
        for (Entity entity : getEntitySet()) {
            sigs.add(entity.getSignature());
        }
        return String.join(",", sigs);
    }
}
