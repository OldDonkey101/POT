package hx.mbt.fsm;

import java.util.*;

public abstract class Sequence{
    protected List<Entity> entityList;

    public List<Entity> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<Entity> entityList) {
        this.entityList = entityList;
    }

    public Sequence(List<Entity> entityList) {
        this.entityList = entityList;
    }

    public <T extends Sequence> T concatenateBy(Entity entity) {
        this.entityList.add(entity);
        return (T) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sequence sequence = (Sequence) o;
        if (sequence.getLength() != this.getLength()) return false;
        for (int i = 0; i < getLength(); i++) {
            if (!sequence.getEntityList().get(i).equals(this.getEntityList().get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        //TODO: 需要重写
        return Objects.hash(getEntityList());
    }

    public abstract <T extends Sequence> T copy();
    public abstract <T extends Sequence> T createEmptySequence();

    public <T extends Sequence> T concatenateBy(T sequence) {
        this.entityList.addAll(sequence.getEntityList());
        return (T) this;
    }

    public String print() {
        String text = "";
        for (Entity entity : getEntityList()) {
            text += entity.print() + ".";
        }
        return text.substring(0,text.length()>0?text.length()-1:0);
    }

    public int getLength() {
        return this.entityList.size();
    }

    public Entity get(int index) {
        if (index >= getLength()) {
            return null;
        } else {
            return this.getEntityList().get(index);
        }
    }

    public boolean hasPrefix(Sequence sequence) {
        if (sequence.getLength() > this.getLength()) {
            return false;
        } else {
            for (int i = 0; i < sequence.getLength(); i++) {
                if (!sequence.get(i).equals(this.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isPrefixOf(Sequence sequence) {
        return sequence.hasPrefix(this);
    }

    public <T extends Sequence> boolean isPrefixOf(Collection<T> sequences) {
        for (T sequence : sequences) {
            if (this.isPrefixOf(sequence)) {
                return true;
            }
        }
        return false;
    }

    public <T extends Sequence> T subSequence(int fromIndex, int toIndex) {
        //TODO: unsafe
        T result = createEmptySequence();
        result.setEntityList(entityList.subList(fromIndex, toIndex));
        return result;
    }

    public <T extends Sequence> T removePrefix(T prefix) {
        if (this.hasPrefix(prefix)) {
            return subSequence(prefix.getLength(), this.getLength());
        } else {
            return (T) this;
        }
    }

    public <T extends Sequence> T getPrefix(int length) {
        assert (length <= getLength());
        return subSequence(0, length);
    }

    public <T extends Sequence> T getLongestCommonPrefix(T seq) {
        //TODO: unsafe
        for (int i = getLength() - 1; i >= 0; i++) {
            if (seq.hasPrefix(getPrefix(i))) {
                return getPrefix(i);
            }
        }
        return createEmptySequence();
    }

    public static <T extends  Sequence> Set<T> partialSet(Set<T> originalSet){
        Set<T> result = new HashSet<>();

        for (T s1 : originalSet) {
            boolean inPartialSet = true;
            for (T s2 : originalSet) {
                if (!s1.equals(s2) && s1.isPrefixOf(s2)) {
                    //s1 is prefix of s2, and s1 /neq s2.
                    //s1 should not be included in partial set
                    inPartialSet = false;
                    break;
                }
            }
            if (inPartialSet) {
                result.add(s1);
            }
        }

        return  result;
    }

    public static <T extends Sequence> T concatenateTwoSequence(T s1, Entity s2) {
        List<Entity> list = new ArrayList<>(s1.getEntityList());
        list.add(s2);
        T result = s1.createEmptySequence();
        result.setEntityList(list);
        return result;
    }

    public static <T extends Sequence> T concatenateTwoSequence(T s1, T s2) {
        List<Entity> list = new ArrayList<>(s1.getEntityList());
        list.addAll(s2.getEntityList());
        T result = s1.createEmptySequence();
        result.setEntityList(list);
        return result;
    }

    public <T extends Sequence> Set< T> concatTwoSequenceSet(Set<T> set1, Set<T> set2) {
        Set<T> resultSet = new HashSet<>();
        for (T s1 : set1) {
            for (T s2 : set2) {
                resultSet.add(concatenateTwoSequence(s1, s2));
            }
        }
        return resultSet;
    }

    public <T extends Entity, T2 extends Sequence> Set<T2> getCupPowerSet(Set<T> X, int n){
        assert (n>0);
        Set<T2> result = new HashSet<>();
        Stack<T2> stack1 = new Stack<>();
        stack1.push(createEmptySequence());
        while (!stack1.isEmpty()) {
            T2 topSequence = stack1.pop();
            if (topSequence.getLength() > 0) result.add(topSequence);//empty sequence is not included.
            if (topSequence.getLength() < n) {
                for (T entity: X) {
                    T2 newSequence = createEmptySequence();
                    newSequence.getEntityList().addAll(topSequence.getEntityList());
                    newSequence.getEntityList().add(entity);
                    stack1.push(newSequence);
                }
            }
        }
        return result;
    }

    public <T extends Entity, T2 extends Sequence> Set<T2> getPowerSet(Set<T> X, int n){
        assert (n>0);
        Set<T2> result = new HashSet<>();
        Stack<T2> stack1 = new Stack<>();
        stack1.push(createEmptySequence());
        while (!stack1.isEmpty()) {
            T2 topSequence = stack1.pop();
            if (topSequence.getLength() == n) result.add(topSequence);
            if (topSequence.getLength() < n) {
                for (T entity: X) {
                    T2 newSequence = createEmptySequence();
                    newSequence.getEntityList().addAll(topSequence.getEntityList());
                    newSequence.getEntityList().add(entity);
                    stack1.push(newSequence);
                }
            }
        }
        return result;
    }
}
