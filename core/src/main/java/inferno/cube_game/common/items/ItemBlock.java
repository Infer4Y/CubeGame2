package inferno.cube_game.common.items;

import inferno.cube_game.common.blocks.Block;

public class ItemBlock extends Item {
    private final Block block;

    public ItemBlock(Block block) {
        super(new Builder(block.getDomain(), block.getRegistryName() + "_item", block.getUnlocalizedName() + "_item"));
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
}

