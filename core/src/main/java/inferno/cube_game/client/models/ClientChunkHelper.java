package inferno.cube_game.client.models;

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
        // Check all six neighbors
        return !chunk.getBlock(x - 1, y, z).isAir() &&
            !chunk.getBlock(x + 1, y, z).isAir() &&
            !chunk.getBlock(x, y - 1, z).isAir() &&
            !chunk.getBlock(x, y + 1, z).isAir() &&
            !chunk.getBlock(x, y, z - 1).isAir() &&
            !chunk.getBlock(x, y, z + 1).isAir();
    }
}
