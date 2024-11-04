package inferno.cube_game.common.registries;

import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.blocks.BlockType;
import inferno.cube_game.common.items.ItemBlock;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class BlockRegistry {
    private static final HashMap<String, Block> BLOCKS = new HashMap<>();


    public static Block AIR_BLOCK;
    public static Block DIRT_BLOCK;
    public static Block GRASS_BLOCK;
    public static Block STONE_BLOCK;
    public static Block BRICK_BLOCK;
    public static Block COBBLESTONE_BLOCK;
    public static Block GLASS_BLOCK;
    public static Block METAL_BLOCK;

    public static void register(Block block) {
        BLOCKS.put(block.getDomain() + ":" + block.getRegistryName(), block);
        ItemRegistry.register(new ItemBlock(block));  // Register corresponding ItemBlock
    }

    public static Block getBlock(String registryName) {
        return BLOCKS.get(registryName);
    }

    public static void registerDefaults() {
        register(AIR_BLOCK = new Block.Builder()
            .setRegistryName("air_block")
            .setUnlocalizedName("tile.air")
            .setAir(true)
            .setSolid(false)
            .setBreakable(false)
            .setHardness(0.0f)
            .setTransparent(true)
            .setType(BlockType.AIR)
            .build());

        register(DIRT_BLOCK = new Block.Builder()
            .setRegistryName("dirt_block")
            .setUnlocalizedName("tile.dirt")
            .setSolid(true)
            .setBreakable(true)
            .setHardness(1.0f)
            .build());

        register(STONE_BLOCK = new Block.Builder()
            .setRegistryName("stone_block")
            .setUnlocalizedName("tile.stone")
            .setSolid(true)
            .setBreakable(true)
            .setHardness(5.0f)
            .setType(BlockType.STONE)
            .build());

        register(BRICK_BLOCK = new Block.Builder()
            .setRegistryName("brick_block")
            .setUnlocalizedName("tile.brick")
            .setSolid(true)
            .setBreakable(true)
            .setHardness(2.0f)
            .setType(BlockType.STONE)
            .build());

        register(GRASS_BLOCK = new Block.Builder()
            .setRegistryName("grass_block")
            .setUnlocalizedName("tile.grass")
            .setSolid(true)
            .setBreakable(true)
            .setHardness(1.2f)
            .setType(BlockType.GRASS)
            .build());

        register(COBBLESTONE_BLOCK = new Block.Builder()
            .setRegistryName("cobblestone_block")
            .setUnlocalizedName("tile.cobblestone")
            .setSolid(true)
            .setBreakable(true)
            .setHardness(1.8f)
            .setType(BlockType.STONE)
            .build());

        register(METAL_BLOCK = new Block.Builder()
            .setRegistryName("metal_block")
            .setUnlocalizedName("tile.metal")
            .setSolid(true)
            .setBreakable(true)
            .setHardness(2.4f)
            .setType(BlockType.METAL)
            .build());

        register(GLASS_BLOCK = new Block.Builder()
            .setRegistryName("glass_block")
            .setUnlocalizedName("tile.glass")
            .setSolid(true)
            .setTransparent(true)
            .setBreakable(true)
            .setHardness(1.8f)
            .setType(BlockType.GLASS)
            .build());

        // Add more default blocks as needed...
    }

    public static List<String> getRegisteredBlockNames() {
        return BLOCKS.keySet().stream().toList();
    }
}


