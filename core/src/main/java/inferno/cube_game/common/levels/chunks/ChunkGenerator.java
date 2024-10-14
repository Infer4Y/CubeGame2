package inferno.cube_game.common.levels.chunks;

import inferno.cube_game.common.registries.BlockRegistry;
import inferno.cube_game.extras.math.OpenSimplex2S;

import java.util.Arrays;
import java.util.stream.IntStream;

public class ChunkGenerator {
    private final long seed;

    public ChunkGenerator(long seed) {
        this.seed = seed;
    }

    // Generates a height map for a chunk
    public int [] generateHeightMap(int chunkX, int chunkY, int chunkZ) {
        int[] oneDimensionalHeightMap = new int[Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE];

        IntStream.rangeClosed(0, oneDimensionalHeightMap.length).parallel().forEach(index -> {
            int z = index % Chunk.CHUNK_SIZE;
            int y = (index / Chunk.CHUNK_SIZE) % Chunk.CHUNK_SIZE;
            int x = index / (Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE);

            double worldX = (chunkX * Chunk.CHUNK_SIZE + x) / 512.0;
            double worldY = (chunkY * Chunk.CHUNK_SIZE + y) / 512.0;
            double worldZ = (chunkZ * Chunk.CHUNK_SIZE + z) / 512.0;

            double mountainNoiseX = OpenSimplex2S.noise2(seed + 300, worldZ , worldY ) * 512;
            double mountainNoiseZ = OpenSimplex2S.noise2(seed + 400, worldZ , worldX ) * 512;
            double mountainNoiseY = OpenSimplex2S.noise2(seed + 200, worldY , worldX ) * 512;

            // Combine multiple layers of noise for varied terrain

            double detailLayer = OpenSimplex2S.noise2(seed + 200, worldX * 4.0, worldZ * 4.0) * 4;

            double baseHeight = OpenSimplex2S.noise4_Fallback(seed, worldX, worldY, worldZ, detailLayer + worldX + worldY + worldZ) * 32;

            double mountainLayer = OpenSimplex2S.noise3_ImproveXZ(seed + 100,
                (worldX + mountainNoiseX) /64.0 ,
                (worldY + mountainNoiseY) /64.0 ,
                (worldZ + mountainNoiseZ) /64.0) * 128;
            int height = (int) (baseHeight
                + mountainLayer
                + detailLayer
                + 64); // Adjust base height
           setHeightAtCoordinate(x, y, z, height, oneDimensionalHeightMap);
           //setHeightAtCoordinate(x, y, z, Math.max(0, height)); // Clamp height to non-negative values

        });

        return oneDimensionalHeightMap;
    }

    private void setHeightAtCoordinate(int x, int y, int z, int height, int[] oneDimensionalHeightMap) {
        if (x < 0 || x >= Chunk.CHUNK_SIZE || y < 0 || y >= Chunk.CHUNK_SIZE || z < 0 || z >= Chunk.CHUNK_SIZE) {
            return;
        }
        oneDimensionalHeightMap[x * Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE + y * Chunk.CHUNK_SIZE + z] = height;
    }

    // Generates a chunk based on its position and precomputed height map
    public Chunk generateChunk(int chunkX, int chunkY, int chunkZ) {
        int[] oneDimensionalHeightMap = generateHeightMap(chunkX, chunkY, chunkZ);
        return new Chunk(seed, chunkX, chunkY, chunkZ, oneDimensionalHeightMap);
    }
}
