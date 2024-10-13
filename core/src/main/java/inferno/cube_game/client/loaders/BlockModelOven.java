package inferno.cube_game.client.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import inferno.cube_game.Main;
import inferno.cube_game.client.models.blocks.BlockModel;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.serialization.Vector3Serializer;

import java.util.HashMap;
import java.util.Map;

/**\
 * BlockModelOven class that loads and caches block models
 * @see BlockModel
 * @see Block
 * @see TextureLoader
 * @see Vector3Serializer
 * @author inferno4you
 */
public class BlockModelOven {
    private static final float SCALE_FACTOR = 1f / 16f; // Define the scaling factor for the block model elements (1/16) to match the block size

    private final HashMap<String, BlockModel> modelCache; // Cache for models to avoid reloading them every time they are needed
    private final Json json; // JSON parser for loading block models

    /**
     * Create a new BlockModelOven
     */
    public BlockModelOven() {
        this.modelCache = new HashMap<>();                                     // Initialize the model cache
        this.json = new Json();                                                // Initialize the JSON parser
        json.setSerializer(Vector3.class, new Vector3Serializer());            // Register the Vector3 serializer
        json.setSerializer(Map.class, new Json.ReadOnlySerializer<Map>() {
            @Override
            public Map read(Json json, JsonValue jsonData, Class type) {
                return json.readValue(HashMap.class, jsonData); // Use HashMap for Map fields
            }
        });
    }

    /**
     * Create or get a block model from the cache
     * @param block Block to get the model for
     * @return BlockModel object
     */
    public BlockModel createOrGetBlockModel(Block block) {
        String blockRegistryName = block.getRegistryName();
        if (modelCache.containsKey(blockRegistryName)) {
            return modelCache.get(blockRegistryName);
        }

        // Load the model config (e.g., .json, .obj, etc.)
        try {
            // Read the block model from the JSON file in the assets folder using the block domain and registry name
            FileHandle blockModelToReadFromJSON = Gdx.files.internal("assets/" + block.getDomain() + "/models/blocks/" + blockRegistryName + ".json");

            // Choose the loader based on the model type
            BlockModel blockModel = json.fromJson(BlockModel.class, blockModelToReadFromJSON);

            // Apply scaling factor to the block model elements
            for (BlockModel.Element element : blockModel.elements) {
                element.from.scl(SCALE_FACTOR);
                element.to.scl(SCALE_FACTOR);
            }

            // Load the textures for the block model if they exist
            if (blockModel.textures != null) {
                blockModel.textures.values().forEach(Main.textureLoader::loadTexture);
            }

            modelCache.put(blockRegistryName, blockModel); // Cache the block model
            return blockModel; // Return the block model
        } catch (SerializationException exception) {
            Gdx.app.error("BlockModelOven", "Failed to load block model for " + blockRegistryName);
        }
        return null;
    }

    public void dispose() {
    }
}
