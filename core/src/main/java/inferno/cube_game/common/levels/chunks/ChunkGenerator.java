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
        int[] oneDimensionalHeightMap = new int[Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE];

        for (int index = 0; index < Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE; index++) {
            int x = index / Chunk.CHUNK_SIZE;
            int z = index % Chunk.CHUNK_SIZE;

            double worldX = (chunkX * Chunk.CHUNK_SIZE + x) / 32.0;
            double worldZ = (chunkZ * Chunk.CHUNK_SIZE + z) / 32.0;

            double worldHillX = worldX / 128.0;
            double worldHillZ = worldZ / 128.0;

            double worldMountainX = worldX / 16.0;
            double worldMountainZ = worldZ / 16.0;

            double worldDetailX = worldX / 4.0;
            double worldDetailZ = worldZ / 4.0;

            double noiseBase = OpenSimplex2S.noise2(seed, worldX, worldZ);
            double hillNoise = OpenSimplex2S.noise2((long) (seed * noiseBase), worldHillX, worldHillZ) * 128;
            double mountainNoise = OpenSimplex2S.noise2((long) (seed * noiseBase), worldMountainX, worldMountainZ) * 256;
            double detailNoise = OpenSimplex2S.noise2((long) (seed * noiseBase), worldDetailX, worldDetailZ) * 4;

            int height = (int) (hillNoise + mountainNoise - detailNoise + 64);
            setHeightAtCoordinate(x, z, height, oneDimensionalHeightMap);
            //setHeightAtCoordinate(x, y, z, Math.max(0, height)); // Clamp height to non-negative values

        }

        return oneDimensionalHeightMap;
    }

    private void setHeightAtCoordinate(int x, int z, int height, int[] oneDimensionalHeightMap) {
        if (x < 0 || x >= Chunk.CHUNK_SIZE || z < 0 || z >= Chunk.CHUNK_SIZE) {
            return;
        }
        oneDimensionalHeightMap[x * Chunk.CHUNK_SIZE + z] = height;
    }

    // Generates a chunk based on its position and precomputed height map
    public Chunk generateChunk(int chunkX, int chunkY, int chunkZ) {
        int[] oneDimensionalHeightMap = generateHeightMap(chunkX, chunkY, chunkZ);
        return new Chunk(chunkX, chunkY, chunkZ, oneDimensionalHeightMap);
    }
}
