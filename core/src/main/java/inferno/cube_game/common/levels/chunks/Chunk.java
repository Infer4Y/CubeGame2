package inferno.cube_game.common.levels.chunks;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import inferno.cube_game.OpenSimplex2S;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.registries.BlockRegistry;

import java.io.Serializable;
import java.util.Random;

public class Chunk implements Serializable {
    public static final int CHUNK_SIZE = 16; // This can be modified for larger vertical chunks
    private Block[] blocks;
    private int chunkX, chunkY, chunkZ;
    private BoundingBox boundingBox;
    private long seed; // Seed for the world generation

    public Chunk(long seed, int chunkX, int chunkY, int chunkZ) {
        this.seed = seed;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        blocks = new Block[CHUNK_SIZE*CHUNK_SIZE*CHUNK_SIZE];

        // Generate terrain
        generateTerrain();
        setBoundingBox();
    }

    private void setBoundingBox() {
        Vector3 min = new Vector3(chunkX * CHUNK_SIZE, chunkY * CHUNK_SIZE, chunkZ * CHUNK_SIZE);
        Vector3 max = new Vector3((chunkX + 1) * CHUNK_SIZE, (chunkY + 1) * CHUNK_SIZE, (chunkZ + 1) * CHUNK_SIZE);
        boundingBox = new BoundingBox(min, max);
    }

    private void generateTerrain() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 0; y < CHUNK_SIZE; y++) {
                // Calculate the world coordinates
                    double worldX = (chunkX * CHUNK_SIZE + x) / 256.0;
                    double worldY = (chunkY * CHUNK_SIZE + y) / 256.0;
                    double worldZ = (chunkZ * CHUNK_SIZE + z) / 256.0;

                    // Calculate the height of the terrain
                    float height = getHeight(worldX, worldY, worldZ);

                    if (worldY < height-.5) {
                        setBlock(x, y, z, BlockRegistry.STONE_BLOCK);
                    } else if (worldY < height) {
                        setBlock(x, y, z, BlockRegistry.DIRT_BLOCK);
                    } else {
                        setBlock(x, y, z, BlockRegistry.AIR_BLOCK);
                    }
                }
            }
        }
    }

    private float getHeight(double worldX, double worldY, double worldZ) {
        float x = OpenSimplex2S.noise2(seed,worldX, worldY);
        float y = OpenSimplex2S.noise2(seed,worldX, worldZ);
        float z = OpenSimplex2S.noise2(seed,worldY, worldZ);

        return Math.abs(OpenSimplex2S.noise4_ImproveXYZ_ImproveXZ(seed, worldX,worldY, worldZ, x+y+z));
    }

    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE) {
            return BlockRegistry.AIR_BLOCK;
        }
        return blocks[x * CHUNK_SIZE * CHUNK_SIZE + y * CHUNK_SIZE + z];
    }

    public void setBlock(int x, int y, int z, Block block) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE) {
            return;
        }
        blocks[x * CHUNK_SIZE * CHUNK_SIZE + y * CHUNK_SIZE + z] = block;
    }

    public int getChunkX() { return chunkX; }
    public int getChunkY() { return chunkY; }
    public int getChunkZ() { return chunkZ; }
    public BoundingBox getBounds() { return boundingBox; }
}
