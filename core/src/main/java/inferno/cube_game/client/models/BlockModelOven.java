package inferno.cube_game.client.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import inferno.cube_game.Main;
import inferno.cube_game.client.models.blocks.BlockModel;
import inferno.cube_game.client.models.loaders.IModelLoader;
import inferno.cube_game.client.models.loaders.JsonModelLoader;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.serialization.Vector3Serializer;

import java.util.HashMap;
import java.util.Map;

// BlockModelOven.java
public class BlockModelOven {
    private HashMap<String, BlockModel> modelCache; // Cache for models
    private Json json;

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

    public BlockModel getBlockModel(Block block) {
        String blockType = block.getRegistryName();
        if (modelCache.containsKey(blockType)) {
            return modelCache.get(blockType);
        }

        // Load the model config (e.g., .json, .obj, etc.)
        try {
            FileHandle configFile = Gdx.files.internal("assets/" + block.getDomain() + "/models/blocks/" + blockType + ".json");
            BlockModel config = json.fromJson(BlockModel.class, configFile);

            // Choose the loader based on the model type
            IModelLoader loader;
            if ("json".equalsIgnoreCase(config.modelType)) {
                loader = new JsonModelLoader();
            } else {
                throw new SerializationException("Unsupported model type: " + config.modelType);
            }

            BlockModel blockModel = loader.loadBlockModel(configFile);

            if (blockModel.textures != null) {
                for (String textureKey : blockModel.textures.values()) {
                    String[] arrayS = textureKey.split(":");
                    String texturePath = block.getDomain() + ":" + arrayS[1];
                    loadTexture(texturePath);
                    // You can store the texture in the BlockModel or a Material if needed
                    // For example: blockModel.textureMap.put(textureKey, texture); (If you need the texture later)
                }
            }
            modelCache.put(blockType, blockModel);

            return blockModel;
        } catch (SerializationException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private void loadTexture(String texturePath) {
        Main.textureLoader.loadTexture(texturePath);
    }

    public void dispose() {
    }

    public HashMap<String, BlockModel> getBlockModels() {
        return modelCache;
    }
}
