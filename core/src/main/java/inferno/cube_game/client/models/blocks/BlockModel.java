// BlockModel.java
package inferno.cube_game.client.models.blocks;

import com.badlogic.gdx.math.Vector3;

import java.util.List;
import java.util.Map;

public class BlockModel {
    // The key is the texture (file specific) name, and the value is the path to the texture file.
    public Map<String, String> textures;
    public List<Element> elements;

    public static class Element {
        public Vector3 from;
        public Vector3 to;
        // Faces are required for each direction. The key is the direction, and the value is the texture (file specific) name.
        public Map<String, String> faces;
    }
}
