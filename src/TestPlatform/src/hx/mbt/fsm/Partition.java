package hx.mbt.fsm;

import jdk.nashorn.internal.ir.BlockLexicalContext;

import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.Collection;

public class Partition{
    ArrayList<Block> blocks;
    public Partition() {
        blocks = new ArrayList<Block>();
    }

    public void addBlock(Block block) {
        this.blocks.add(block);
    }

    public void addBlocks(Collection<Block> blocks) {
        this.blocks.addAll(blocks);
    }

    public ArrayList<Block> getBlocks() {
        return blocks;
    }
}
