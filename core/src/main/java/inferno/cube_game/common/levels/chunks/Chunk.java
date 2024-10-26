package inferno.cube_game.common.levels.chunks;

import com.badlogic.gdx.math.collision.BoundingBox;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.registries.BlockRegistry;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.stream.IntStream;

public class Chunk implements Serializable {
    public static final int CHUNK_SIZE = 16;
    private byte[] blockPaletteIndices; // Store indices instead of block IDs
    LinkedHashMap<Byte, Block> palette; // The palette maps indices to blocks
    private int chunkX, chunkY, chunkZ;

    // Primary Constructor - Generates new terrain
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

    // Secondary Constructor - Loads from saved data
    public Chunk(int chunkX, int chunkY, int chunkZ, LinkedHashMap<Byte, Block> palette, byte[] blockPaletteIndices) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.palette = palette;
        this.blockPaletteIndices = blockPaletteIndices;
    }

    private void generateTerrain(int[] heightMap) {
        IntStream.range(0, CHUNK_SIZE).forEach(x -> {
            IntStream.range(0, CHUNK_SIZE).forEach(z -> {
                int height = heightMap[x * CHUNK_SIZE + z];
                int chunkYOffset = (chunkY * CHUNK_SIZE);

                IntStream.range(0, CHUNK_SIZE).forEach(y -> {
                    Block block = BlockRegistry.AIR_BLOCK;
                    if (y + chunkYOffset == height) {
                        block = BlockRegistry.GRASS_BLOCK;
                    } else if (y + chunkYOffset < height - 1 && y + chunkYOffset > height - 4) {
                        block = BlockRegistry.DIRT_BLOCK;
                    } else if (y + chunkYOffset <= height - 4) {
                        block = BlockRegistry.STONE_BLOCK;
                    }

                    if (block.isAir()) return;

                    byte paletteIndex = getOrAddToPalette(block);
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
        byte paletteIndex = getOrAddToPalette(block);
        blockPaletteIndices[x * CHUNK_SIZE * CHUNK_SIZE + y * CHUNK_SIZE + z] = paletteIndex;
    }

    private byte getOrAddToPalette(Block block) {
        if (palette.containsValue(block)) {
            return palette.entrySet().stream()
                .filter(entry -> entry.getValue().equals(block))
                .map(entry -> entry.getKey())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Block not found in palette!"));
        }

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

    public boolean onlyAir() {
        return Arrays.equals(blockPaletteIndices, new byte[blockPaletteIndices.length]);
    }

    public boolean hasNoAirInAnyLayer() {
        for (byte block : blockPaletteIndices) {
            if (block == -1) return false;
        }
        return true;
    }

    public byte[] getBlocks() {
        return blockPaletteIndices.clone();
    }

    public LinkedHashMap<Byte, Block> getPalette() {
        return palette;
    }
}
