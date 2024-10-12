package inferno.cube_game.client.models.loaders;

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

// BlockModelOven.java
public class BlockModelOven {
    private static final float SCALE_FACTOR = 1f / 16f; // Define the scaling factor

    private final HashMap<String, BlockModel> modelCache; // Cache for models
    private final Json json;

    public BlockModelOven() {
        this.modelCache = new HashMap<>();
        this.json = new Json();
        json.setSerializer(Vector3.class, new Vector3Serializer());
        json.setSerializer(Map.class, new Json.ReadOnlySerializer<Map>() {
            @Override
            public Map read(Json json, JsonValue jsonData, Class type) {
                return json.readValue(HashMap.class, jsonData); // Use HashMap for Map fields
            }
        });
    }

    public BlockModel createOrGetBlockModel(Block block) {
        String blockType = block.getRegistryName();
        if (modelCache.containsKey(blockType)) {
            return modelCache.get(blockType);
        }

        // Load the model config (e.g., .json, .obj, etc.)
        try {
            FileHandle blockModelToReadFromJSON = Gdx.files.internal("assets/" + block.getDomain() + "/models/blocks/" + blockType + ".json");

            // Choose the loader based on the model type
            BlockModel blockModel = json.fromJson(BlockModel.class, blockModelToReadFromJSON);
            // Apply scaling factor if needed
            for (BlockModel.Element element : blockModel.elements) {
                element.from.scl(SCALE_FACTOR);
                element.to.scl(SCALE_FACTOR);
            }

            // Load the textures
            if (blockModel.textures != null) {
                blockModel.textures.values().forEach(Main.textureLoader::loadTexture);
            }

            modelCache.put(blockType, blockModel);
            return blockModel;
        } catch (SerializationException exception) {
            Gdx.app.error("BlockModelOven", "Failed to load block model for " + blockType);
        }
        return null;
    }

    public void dispose() {
    }
}
