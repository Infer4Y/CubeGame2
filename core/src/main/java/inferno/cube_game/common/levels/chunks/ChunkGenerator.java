package inferno.cube_game.common.levels.chunks;

import inferno.cube_game.OpenSimplex2S;
import inferno.cube_game.common.levels.chunks.Chunk;

public class ChunkGenerator {
    private long seed;

    public ChunkGenerator(long seed) {
        this.seed = seed;
    }

    // Generates a height map for a chunk
    public int[][] generateHeightMap(int chunkX, int chunkZ) {
        int[][] heightMap = new int[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];

        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                double worldX = (chunkX * Chunk.CHUNK_SIZE + x) / 256.0;
                double worldZ = (chunkZ * Chunk.CHUNK_SIZE + z) / 256.0;

                double mountainNoiseX = OpenSimplex2S.noise2(seed + 300, worldX / 20.0, worldZ / 20.0) * 20;
                double mountainNoiseZ = OpenSimplex2S.noise2(seed + 400, worldZ / 20.0, worldX / 20.0) * 20;

                // Combine multiple layers of noise for varied terrain
                double baseHeight = OpenSimplex2S.noise2(seed, worldX, worldZ) * 20;
                double mountainLayer = OpenSimplex2S.noise2(seed + 100, (worldX + mountainNoiseX) / 15.0, (worldZ + mountainNoiseZ) / 15.0) * 20;
                double detailLayer = OpenSimplex2S.noise2(seed + 200, worldX * 4.0, worldZ * 4.0) * 2;

                int height = (int) (baseHeight + mountainLayer + detailLayer  + 50); // Adjust base height
                heightMap[x][z] = Math.max(0, height); // Clamp height to non-negative values
            }
        }

        return heightMap;
    }

    // Generates a chunk based on its position and precomputed height map
    public Chunk generateChunk(int chunkX, int chunkY, int chunkZ) {
        int[][] heightMap = generateHeightMap(chunkX, chunkZ);
        return new Chunk(seed, chunkX, chunkY, chunkZ, heightMap);
    }
}
