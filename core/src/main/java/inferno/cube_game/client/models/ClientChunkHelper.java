package inferno.cube_game.client.models;

import inferno.cube_game.common.levels.chunks.Chunk;

public class ClientChunkHelper {
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
