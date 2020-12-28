package hx.mbt.fsm;

import jdk.nashorn.internal.runtime.regexp.joni.constants.EncloseType;

import java.util.Objects;

public abstract class Entity{
    private String signature;

    public Entity(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String print() {
        return this.signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(getSignature(), entity.getSignature());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSignature());
    }
}
