// BlockModel.java
package inferno.cube_game.client.models.blocks;

import com.badlogic.gdx.math.Vector3;

import java.util.List;
import java.util.Map;

/**
 * BlockModel class that represents a block model.
 * @see Element
 * @author inferno4you
 */
public class BlockModel {
    /**
     * The key is the texture (file specific) name, and the value is the path to the texture file.
     * Example: "north": "textures/blocks/dirt.png"
     * The key is the texture key, and the value is the texture (file specific) name.
     */
    public Map<String, String> textures;

    /**
     * List of elements that make up the block model.
     * Each element represents a cube in the block model.
     */
    public List<Element> elements;

    /**
     * Element class that represents a cube element in the block model.
     * specify the cube's dimensions and textures for each face.
     */
    public static class Element {
        // From and to are the two corners of the cube. The values are relative to the block size (0-16).
        // Example: from: [0, 0, 0], to: [16, 16, 16] would be a full cube.
        public Vector3 from;
        public Vector3 to;

        // Faces are required for each direction. The key is the direction, and the value is the texture (file specific) name.
        // Example: "north": "north"
        // Example: "south": "sides"
        // Example: "east": "electric_fence"
        public Map<String, String> faces;
    }
}
