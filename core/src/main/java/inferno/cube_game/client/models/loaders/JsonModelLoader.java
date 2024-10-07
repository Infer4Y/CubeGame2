package inferno.cube_game.client.models.loaders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import inferno.cube_game.client.models.blocks.BlockModel;
import inferno.cube_game.client.models.blocks.Face;
import inferno.cube_game.client.models.blocks.FaceSerializer;
import inferno.cube_game.common.serialization.Vector3Serializer;

import java.util.HashMap;
import java.util.Map;

public class JsonModelLoader implements IModelLoader {
    private static final float SCALE_FACTOR = 1f / 16f; // Define the scaling factor

    @Override
    public BlockModel loadBlockModel(FileHandle fileHandle) {
        Json json = new Json();
        json.setSerializer(Vector3.class, new Vector3Serializer());
        json.addClassTag("face", Face.class);
        json.setSerializer(Map.class, new Json.ReadOnlySerializer<>() {
            @Override
            public Map read(Json json, JsonValue jsonData, Class type) {
                return json.readValue(HashMap.class, jsonData); // Use HashMap for Map fields
            }
        });
        BlockModel blockModel = json.fromJson(BlockModel.class, fileHandle);
        // Apply scaling factor if needed
        for (BlockModel.Element element : blockModel.elements) {
            element.from.scl(SCALE_FACTOR);
            element.to.scl(SCALE_FACTOR);
        }
        return blockModel;
    }
}
