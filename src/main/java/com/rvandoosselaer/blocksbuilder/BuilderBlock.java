package com.rvandoosselaer.blocksbuilder;

import com.rvandoosselaer.blocks.Block;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A builder block is a collection of blocks with a similar shape. Only 1 block of a shape is visible in the blocks
 * window. The default block or 'start' block is calculated each time a block is added. The default block can be
 * retrieved with {@link #getBlock()}. The {@link #getNextBlock()} will fetch the next block in the list or return to
 * the first one.
 *
 * @author: rvandoosselaer
 */
@RequiredArgsConstructor
public class BuilderBlock {

    @Getter
    private final String name;
    private List<Block> blocks = new ArrayList<>();
    private int index = 0;

    public void addBlock(Block block) {
        blocks.add(block);
        calculateDefaultIndex();
    }

    /**
     * Returns the current block
     */
    public Block getBlock() {
        return blocks.isEmpty() ? null : blocks.get(index);
    }

    /**
     * Returns the next block
     */
    public Block getNextBlock() {
        index++;
        if (index >= blocks.size()) {
            index = 0;
        }

        return blocks.isEmpty() ? null : blocks.get(index);
    }

    /**
     * Returns the next block starting from the given block.
     *
     * @param block
     */
    public Optional<Block> getNextBlock(Block block) {
        int blockIndex = blocks.indexOf(block);
        if (blockIndex >= 0) {
            int nextBlockIndex = blockIndex + 1;
            if (nextBlockIndex >= blocks.size()) {
                nextBlockIndex = 0;
            }

            return Optional.of(blocks.get(nextBlockIndex));
        }

        return Optional.empty();
    }

    /**
     * Reset the index to the default block.
     */
    public void reset() {
        calculateDefaultIndex();
    }

    /**
     * calculates and sets the default index. This should be called each time a block is added to the list. When the
     * list contains a block with a name ending with '_up', this will be the default block. If such block does not exist
     * the block ending with '_west' will be the default block.
     */
    private void calculateDefaultIndex() {
        int upIndex = -1, westIndex = -1;
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            if (block.getShape().endsWith("_up")) {
                upIndex = i;
            } else if (block.getShape().endsWith("_west")) {
                westIndex = i;
            }
        }

        if (upIndex >= 0) {
            index = upIndex;
        } else if (westIndex >= 0) {
            index = westIndex;
        }
    }

}
