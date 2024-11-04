package inferno.cube_game.common.levels.chunks;

import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.levels.World;
import inferno.cube_game.common.registries.BlockRegistry;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.IntStream;

public class Chunk implements Serializable {
    public static final int CHUNK_SIZE = 16;
    private short[] blockPaletteIndices;
    private Block[] palette; // Array-based palette
    private short paletteSize;
    private int chunkX, chunkY, chunkZ;

    public Chunk(int chunkX, int chunkY, int chunkZ, int[] heightMap) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.blockPaletteIndices = new short[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
        Arrays.fill(blockPaletteIndices, (short) 0);
        this.palette = new Block[65535];
        this.palette[0] = BlockRegistry.AIR_BLOCK; // Assign index 0 to AIR_BLOCK
        this.paletteSize = 1;

        generateTerrain(heightMap);
    }

    private void generateTerrain(int[] heightMap) {
        for (int index = 0; index < Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE; index++) {
            int x = index / Chunk.CHUNK_SIZE;
            int z = index % Chunk.CHUNK_SIZE;
            int height = heightMap[x * CHUNK_SIZE + z];
            int chunkYOffset = (chunkY * CHUNK_SIZE);

            for (int y = 0; y < CHUNK_SIZE; y++) {
                Block block = BlockRegistry.AIR_BLOCK;
                if (y + chunkYOffset == height) {
                    block = BlockRegistry.GRASS_BLOCK;
                } else if (y + chunkYOffset <= height - 1 && y + chunkYOffset > height - 4) {
                    block = BlockRegistry.DIRT_BLOCK;
                } else if (y + chunkYOffset <= height - 4) {
                    block = BlockRegistry.STONE_BLOCK;
                }

                if (block.isAir()) continue;

                short paletteIndex = getOrAddToPalette(block);
                blockPaletteIndices[x * CHUNK_SIZE * CHUNK_SIZE + y * CHUNK_SIZE + z] = paletteIndex;
            }
        }
    }

    public boolean isFaceNotVisible(int x, int y, int z, String face) {
        Block neighbor = switch (face) {
            case "top" -> getBlock(x, y + 1, z);
            case "bottom" -> getBlock(x, y - 1, z);
            case "north" -> getBlock(x, y, z - 1);
            case "south" -> getBlock(x, y, z + 1);
            case "west" -> getBlock(x - 1, y, z);
            case "east" -> getBlock(x + 1, y, z);
            default -> null;
        };
        return neighbor != null && (!neighbor.isAir());
    }

    public boolean canCullBlock(int x, int y, int z) {
        Block topBlock = getBlock(x, y + 1, z);
        Block bottomBlock = getBlock(x, y - 1, z);
        Block frontBlock = getBlock(x, y , z + 1);
        Block backBlock = getBlock(x, y , z - 1);
        Block leftBlock = getBlock(x - 1, y, z);
        Block rightBlock = getBlock(x + 1, y, z);

        boolean result;

        result = !topBlock.isAir() &&
            !bottomBlock.isAir() &&
            !frontBlock.isAir() &&
            !backBlock.isAir() &&
            !leftBlock.isAir() &&
            !rightBlock.isAir();

        // Check all six neighbors
        return result;
    }

    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE) {
            return BlockRegistry.AIR_BLOCK;
        }
        short paletteIndex = blockPaletteIndices[x * CHUNK_SIZE * CHUNK_SIZE + y * CHUNK_SIZE + z];
        return palette[paletteIndex];
    }

    public void setBlock(int x, int y, int z, Block block) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE) {
            return;
        }
        short paletteIndex = getOrAddToPalette(block);
        blockPaletteIndices[x * CHUNK_SIZE * CHUNK_SIZE + y * CHUNK_SIZE + z] = paletteIndex;
    }

    private short getOrAddToPalette(Block block) {
        for (short i = 0; i < paletteSize; i++) {
            if (palette[i] == block) {
                return i;
            }
        }

        if (paletteSize >= 256) {
            throw new RuntimeException("Palette overflow in chunk!");
        }

        palette[paletteSize] = block;
        return paletteSize++;
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
        for (short block : blockPaletteIndices) {
            if (block == 0) return false;
        }
        return true;
    }

    public short[] getBlocks() {
        return blockPaletteIndices.clone();
    }

    public Block[] getPalette() {
        return palette;
    }
}
