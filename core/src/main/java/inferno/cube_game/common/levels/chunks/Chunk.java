package inferno.cube_game.common.levels.chunks;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.levels.World;
import inferno.cube_game.common.registries.BlockRegistry;
import inferno.cube_game.extras.utils.MapUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class Chunk implements Serializable {
    public static final int CHUNK_SIZE = 16;
    private byte[] blockPaletteIndices; // Store indices instead of block IDs
    private LinkedHashMap<Byte, Block> palette;   // The palette maps indices to blocks
    private int chunkX, chunkY, chunkZ;
    private BoundingBox boundingBox;

    public Chunk(int chunkX, int chunkY, int chunkZ, int[] heightMap) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.blockPaletteIndices = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
        Arrays.fill(blockPaletteIndices, (byte) -1);
        this.palette = new LinkedHashMap<>(256);
        this.palette.put((byte) -1, BlockRegistry.AIR_BLOCK);

        generateTerrain(heightMap);
    }


    private void generateTerrain(int[] heightMap) {
        IntStream.range(0, CHUNK_SIZE).forEach(x -> {
            IntStream.range(0, CHUNK_SIZE).forEach(z -> {
                int height = heightMap[x*CHUNK_SIZE + z];
                int chunkYOffset = (chunkY * CHUNK_SIZE);

                IntStream.range(0, CHUNK_SIZE).forEach(y -> {
                    Block block = BlockRegistry.AIR_BLOCK;
                    if ( y + chunkYOffset == height) {
                        block = BlockRegistry.GRASS_BLOCK;
                    } else if (y+ chunkYOffset <= height-1 && y+ chunkYOffset >= height-4) {
                        block = BlockRegistry.DIRT_BLOCK;
                    } else if (y+ chunkYOffset <= height-4) {
                        block = BlockRegistry.STONE_BLOCK;
                    }

                    if (block.isAir()) return;

                    byte paletteIndex = getOrAddToPalette(block); // Get palette index for the block
                    blockPaletteIndices[x * CHUNK_SIZE * CHUNK_SIZE + y * CHUNK_SIZE + z] = paletteIndex;
                });
            });
        });
    }

    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE) {
            return BlockRegistry.AIR_BLOCK;
        }
        byte paletteIndex = blockPaletteIndices[x * CHUNK_SIZE * CHUNK_SIZE + y * CHUNK_SIZE + z];
        return palette.get(paletteIndex); // Get block from palette
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
        if (palette.containsValue(block)) {
            Set<Byte> bytesInPallet = MapUtils.getKeysByValue(palette, block);
            if (!bytesInPallet.isEmpty()) return bytesInPallet.iterator().next();
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
        return Arrays.equals(blockPaletteIndices, World.emptyChunk);
    }

    public boolean hasNoAirInAnyLayer() {
        for (byte block : blockPaletteIndices) {
            if(block == -1) return false;
        }
        return true;
    }

}
