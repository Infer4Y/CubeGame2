// BlockModel.java
package inferno.cube_game.client.models.blocks;

import com.badlogic.gdx.math.Vector3;

import java.util.List;
import java.util.Map;

public class BlockModel {
    public String parent;
    public Map<String, String> textures;
    public List<Element> elements;
    public String modelType;

    public static class Element {
        public Vector3 from;
        public Vector3 to;
        public Map<String, String> faces;
    }

}
