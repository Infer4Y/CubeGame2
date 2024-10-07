package inferno.cube_game.common.serialization;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class Vector3Serializer implements Json.Serializer<Vector3> {

    @Override
    public void write(Json json, Vector3 object, Class knownType) {
        // Serialize the Vector3 as an array [x, y, z]
        json.writeArrayStart();
        json.writeValue(object.x);
        json.writeValue(object.y);
        json.writeValue(object.z);
        json.writeArrayEnd();
    }

    @Override
    public Vector3 read(Json json, JsonValue jsonData, Class type) {
        // Deserialize the array [x, y, z] into a Vector3
        if (jsonData.isArray()) {
            float x = jsonData.get(0).asFloat();
            float y = jsonData.get(1).asFloat();
            float z = jsonData.get(2).asFloat();
            return new Vector3(x, y, z);
        }
        return null; // Return null if deserialization fails
    }
}

