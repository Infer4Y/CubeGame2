package inferno.cube_game.common.levels.chunks;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.registries.BlockRegistry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class Chunk implements Serializable {
    public static final int CHUNK_SIZE = 24;
    private byte[] blockPaletteIndices; // Store indices instead of block IDs
    private ConcurrentHashMap<Byte, Block> palette;   // The palette maps indices to blocks
    private int chunkX, chunkY, chunkZ;
    private BoundingBox boundingBox;

    public Chunk(int chunkX, int chunkY, int chunkZ, int[] heightMap) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.blockPaletteIndices = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
        this.palette = new ConcurrentHashMap<>();
        this.palette.put((byte) 0, BlockRegistry.AIR_BLOCK);

        generateTerrain(heightMap);
        setBoundingBox();
    }

    private void setBoundingBox() {
        Vector3 min = new Vector3(chunkX * CHUNK_SIZE, chunkY * CHUNK_SIZE, chunkZ * CHUNK_SIZE);
        Vector3 max = new Vector3((chunkX + 1) * CHUNK_SIZE, (chunkY + 1) * CHUNK_SIZE, (chunkZ + 1) * CHUNK_SIZE);
        boundingBox = new BoundingBox(min, max);
    }

    private void generateTerrain(int[] heightMap) {
        IntStream.range(0, CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE).parallel().forEach(index -> {
            int z = index % Chunk.CHUNK_SIZE;
            int y = (index / Chunk.CHUNK_SIZE) % Chunk.CHUNK_SIZE;
            int x = index / (Chunk.CHUNK_SIZE * CHUNK_SIZE);

            int height = getHeightAtBlockPosition(x, y, z, heightMap); // Get the height from the precomputed map

            if (chunkY * CHUNK_SIZE + y < height) {
                setBlock(x, y, z, BlockRegistry.GRASS_BLOCK);
            } else {
                setBlock(x, y, z, BlockRegistry.AIR_BLOCK);
            }
        });
    }

    private int getHeightAtBlockPosition(int x, int y, int z, int[] oneDimensionalHeightMap) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE) {
            return 0;
        }
        return oneDimensionalHeightMap[x * CHUNK_SIZE * CHUNK_SIZE + y * CHUNK_SIZE + z];
    }

    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE) {
            return BlockRegistry.AIR_BLOCK;
        }
        byte paletteIndex = blockPaletteIndices[x * CHUNK_SIZE * CHUNK_SIZE + y * CHUNK_SIZE + z];
        return palette.getOrDefault(paletteIndex, BlockRegistry.AIR_BLOCK); // Get block from palette
    }

    public void setBlock(int x, int y, int z, Block block) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE) {
            return;
        }
        byte paletteIndex = getOrAddToPalette(block); // Get palette index for the block
        blockPaletteIndices[x * CHUNK_SIZE * CHUNK_SIZE + y * CHUNK_SIZE + z] = paletteIndex;
    }

    private byte getOrAddToPalette(Block block) {
        // Search for the block in the palette
        for (Map.Entry<Byte, Block> entry : palette.entrySet()) {
            if (entry.getValue().equals(block)) {
                return entry.getKey();
            }
        }

        // If not found, add it to the palette
        byte newPaletteIndex = (byte) palette.size();
        if (newPaletteIndex >= 256) {
            throw new RuntimeException("Palette overflow in chunk!"); // Palette can store max 256 blocks
        }
        palette.put(newPaletteIndex, block);
        return newPaletteIndex;
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

    public boolean onlyAir() {
        return IntStream.range(0, blockPaletteIndices.length).allMatch(i -> blockPaletteIndices[i] == (byte) 0);
    }

    public boolean hasAirInFirstLayer() {
        return IntStream.range(0, CHUNK_SIZE).parallel().anyMatch(x -> IntStream.range(0, CHUNK_SIZE).parallel().anyMatch(z -> getBlock(x, 0, z).isAir()));
    }
}
