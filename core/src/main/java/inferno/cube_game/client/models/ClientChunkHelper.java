package inferno.cube_game.client.models;

import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.levels.chunks.Chunk;

public class ClientChunkHelper {
    /**
     * Check if a block can be culled (hidden) based on its neighbors
     * @param chunk Chunk the block is in
     * @param x X coordinate of the block
     * @param y Y coordinate of the block
     * @param z Z coordinate of the block
     * @return True if the block can be culled, false otherwise
     */
    public static boolean canCullBlock(Chunk chunk, int x, int y, int z) {
        Block topBlock = chunk.getBlock(x, y +1, z);
        Block bottomBlock = chunk.getBlock(x, y - 1, z);
        Block frontBlock = chunk.getBlock(x, y , z +1);
        Block backBlock = chunk.getBlock(x, y , z -1);
        Block leftBlock = chunk.getBlock(x - 1, y, z);
        Block rightBlock = chunk.getBlock(x + 1, y, z);

        boolean result;

        result = !topBlock.isAir() &&
            !bottomBlock.isAir() &&
            !frontBlock.isAir() &&
            !backBlock.isAir() &&
            !leftBlock.isAir() &&
            !rightBlock.isAir();

        result = !topBlock.isTransparent() &&
            !bottomBlock.isTransparent() &&
            !frontBlock.isTransparent() &&
            !backBlock.isTransparent() &&
            !leftBlock.isTransparent() &&
            !rightBlock.isTransparent() && result;

        // Check all six neighbors
        return result;
    }
}
