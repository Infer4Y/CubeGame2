package inferno.cube_game.common.levels.chunks;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import inferno.cube_game.OpenSimplex2S;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.registries.BlockRegistry;

import java.io.Serializable;
import java.util.stream.IntStream;

public class Chunk implements Serializable {
    public static final int CHUNK_SIZE = 16;
    private Block[] blocks;
    private int chunkX, chunkY, chunkZ;
    private BoundingBox boundingBox;
    private long seed;
    private int[][] heightMap; // Precomputed height map for faster generation

    public Chunk(long seed, int chunkX, int chunkY, int chunkZ, int[][] heightMap) {
        this.seed = seed;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.blocks = new Block[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
        this.heightMap = heightMap;

        generateTerrain();
        setBoundingBox();
    }

    private void setBoundingBox() {
        Vector3 min = new Vector3(chunkX * CHUNK_SIZE, chunkY * CHUNK_SIZE, chunkZ * CHUNK_SIZE);
        Vector3 max = new Vector3((chunkX + 1) * CHUNK_SIZE, (chunkY + 1) * CHUNK_SIZE, (chunkZ + 1) * CHUNK_SIZE);
        boundingBox = new BoundingBox(min, max);
    }

    private void generateTerrain() {
        IntStream.range(0, CHUNK_SIZE).parallel().forEach(x -> {
            IntStream.range(0, CHUNK_SIZE).parallel().forEach(z -> {
                int height = heightMap[x][z]; // Get the height from the precomputed map

                IntStream.range(0, CHUNK_SIZE).parallel().forEach(y -> {
                    if (chunkY * CHUNK_SIZE + y < height - 1) {
                        if ((y % 2) == 0) setBlock(x, y, z, BlockRegistry.BRICK_BLOCK);
                        else setBlock(x, y, z, BlockRegistry.STONE_BLOCK);
                    } else if (chunkY * CHUNK_SIZE + y < height) {
                        setBlock(x, y, z, BlockRegistry.GRASS_BLOCK);
                    } else {
                        setBlock(x, y, z, BlockRegistry.AIR_BLOCK);
                    }
                });
            });
        });
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

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkY() {
        return chunkY;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public BoundingBox getBounds() {
        return boundingBox;
    }
}
