package inferno.cube_game.client.models.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;

public class TextureLoader {
    // Cache for textures to avoid reloading them every time they are needed
    private final HashMap<String, Texture> textureCache = new HashMap<>();

    /**
     * load a texture from the assets folder and cache it if it hasn't been loaded yet
     * @param texturePath
     * @return
     */
    public Texture loadTexture(String texturePath) {
        final String[] splitTexturePath = texturePath.split(":"); // Split the texture path into domain and texture name
        String domain = splitTexturePath[0]; // Get the domain
        String textureName = splitTexturePath[1]; // Get the texture name

        if (!textureCache.containsKey(texturePath)) {
            String path = "assets/" + domain + "/textures/" + textureName; // Construct the path to the texture file
            Texture texture = new Texture(Gdx.files.internal(path));       // Load the texture from the file
            textureCache.put(texturePath, texture);                        // Cache the texture
        }

        return textureCache.get(texturePath); // Return the texture from the cache
    }

    // Optional: Handle .mcmeta for animations
    public void loadTextureMeta(FileHandle mcmetaFile) {
        // Parse .mcmeta for animation details
    }

    public void dispose(){ // Dispose of all textures in the cache
        for(Texture texture : textureCache.values()) texture.dispose();
    }
}

