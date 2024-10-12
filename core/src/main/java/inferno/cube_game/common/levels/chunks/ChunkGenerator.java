package inferno.cube_game.common.levels.chunks;

import inferno.cube_game.extras.math.OpenSimplex2S;

import java.util.stream.IntStream;

public class ChunkGenerator {
    private long seed;

    public ChunkGenerator(long seed) {
        this.seed = seed;
    }

    // Generates a height map for a chunk
    public int[][][] generateHeightMap(int chunkX, int chunkY, int chunkZ) {
        int[][][] heightMap = new int[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];

        IntStream.range(0, Chunk.CHUNK_SIZE).parallel().forEach(x -> {
            IntStream.range(0, Chunk.CHUNK_SIZE).parallel().forEach(y -> {
                IntStream.range(0, Chunk.CHUNK_SIZE).parallel().forEach(z -> {
                    double worldX = (chunkX * Chunk.CHUNK_SIZE + x) / 512.0;
                    double worldY = (chunkX * Chunk.CHUNK_SIZE + y) / 512.0;
                    double worldZ = (chunkZ * Chunk.CHUNK_SIZE + z) / 512.0;

                    double mountainNoiseX = OpenSimplex2S.noise2(seed + 300, worldZ / 64.0, worldY / 64.0) * 32;
                    double mountainNoiseZ = OpenSimplex2S.noise2(seed + 400, worldZ / 64.0, worldX / 64.0) * 32;
                    double mountainNoiseY = OpenSimplex2S.noise2(seed + 200, worldY / 64.0, worldX / 64.0) * 32;

                    // Combine multiple layers of noise for varied terrain
                    double baseHeight = OpenSimplex2S.noise3_ImproveXZ(seed, worldX, worldY, worldZ) * 32;
                    double mountainLayer = OpenSimplex2S.noise3_ImproveXZ(seed + 100,
                        (worldX + mountainNoiseX) / 150.0,
                        (worldX + mountainNoiseY) / 150.0,
                        (worldZ + mountainNoiseZ) / 150.0) * 256;
                    double detailLayer = OpenSimplex2S.noise2(seed + 200, worldX * 4.0, worldZ * 4.0) * 4;

                    int height = (int) (baseHeight * mountainLayer - detailLayer + 64); // Adjust base height
                    heightMap[x][y][z] = Math.max(0, height); // Clamp height to non-negative values
                });
            });
        });

        return heightMap;
    }

    // Generates a chunk based on its position and precomputed height map
    public Chunk generateChunk(int chunkX, int chunkY, int chunkZ) {
        int[][][] heightMap = generateHeightMap(chunkX, chunkY, chunkZ);
        return new Chunk(seed, chunkX, chunkY, chunkZ, heightMap);
    }
}
