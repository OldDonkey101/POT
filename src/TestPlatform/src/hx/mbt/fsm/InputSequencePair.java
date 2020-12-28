package hx.mbt.fsm;

import java.util.Objects;

public class InputSequencePair{
    InputSequence seq1;
    InputSequence seq2;

    public InputSequencePair(InputSequence seq1, InputSequence seq2) {
        this.seq1 = seq1;
        this.seq2 = seq2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputSequencePair that = (InputSequencePair) o;
        return Objects.equals(seq1, that.seq1) && Objects.equals(seq2, that.seq2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seq1, seq2);
    }
}