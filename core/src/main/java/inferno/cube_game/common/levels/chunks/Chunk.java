package inferno.cube_game.common.levels.chunks;

import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.levels.World;
import inferno.cube_game.common.registries.BlockRegistry;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.IntStream;

public class Chunk implements Serializable {
    public static final int CHUNK_SIZE = 16;
    private byte[] blockPaletteIndices;
    private Block[] palette; // Array-based palette
    private int paletteSize;
    private int chunkX, chunkY, chunkZ;

    public Chunk(int chunkX, int chunkY, int chunkZ, int[] heightMap) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.blockPaletteIndices = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
        Arrays.fill(blockPaletteIndices, (byte) 0);
        this.palette = new Block[256];
        this.palette[0] = BlockRegistry.AIR_BLOCK; // Assign index 0 to AIR_BLOCK
        this.paletteSize = 1;

        generateTerrain(heightMap);
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
                    } else if (y + chunkYOffset <= height - 1 && y + chunkYOffset > height - 4) {
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
        return palette[paletteIndex];
    }

    public void setBlock(int x, int y, int z, Block block) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE) {
            return;
        }
        byte paletteIndex = getOrAddToPalette(block);
        blockPaletteIndices[x * CHUNK_SIZE * CHUNK_SIZE + y * CHUNK_SIZE + z] = paletteIndex;
    }

    private byte getOrAddToPalette(Block block) {
        for (byte i = 0; i < paletteSize; i++) {
            if (palette[i] == block) {
                return i;
            }
        }

        if (paletteSize >= 256) {
            throw new RuntimeException("Palette overflow in chunk!");
        }

        palette[paletteSize] = block;
        return (byte) paletteSize++;
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
        return Arrays.equals(blockPaletteIndices, World.emptyChunk);
    }

    public boolean hasNoAirInAnyLayer() {
        for (byte block : blockPaletteIndices) {
            if (block == 0) return false;
        }
        return true;
    }

    public byte[] getBlocks() {
        return blockPaletteIndices.clone();
    }

    public Block[] getPalette() {
        return palette;
    }
}
