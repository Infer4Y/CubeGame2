package inferno.cube_game.client.models.blocks;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class FaceSerializer implements Json.Serializer<Face> {
    @Override
    public void write(Json json, Face face, Class knownType) {
        json.writeObjectStart();
        json.writeValue("texture", face.texture);
        json.writeObjectEnd();
    }


    @Override
    public Face read(Json json, JsonValue jsonData, Class type) {
        Face face = new Face();
        face.texture = jsonData.getString("texture");
        return face;
    }
}
