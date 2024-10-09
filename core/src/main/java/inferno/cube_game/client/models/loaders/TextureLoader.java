package inferno.cube_game.client.models.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;

public class TextureLoader {
    private HashMap<String, Texture> textureCache = new HashMap<>();

    public Texture loadTexture(String texturePath) {
        final String[] splitTexturePath = texturePath.split(":");
        String domain = splitTexturePath[0];
        String textureName = splitTexturePath[1];

        if (!textureCache.containsKey(texturePath)) {
            String path = "assets/" + domain + "/textures/" + textureName;
            Texture texture = new Texture(Gdx.files.internal(path));
            textureCache.put(texturePath, texture);
        }

        return textureCache.get(texturePath);
    }

    // Optional: Handle .mcmeta for animations
    public void loadTextureMeta(FileHandle mcmetaFile) {
        // Parse .mcmeta for animation details
    }

    public void dispose(){
        for(Texture texture : textureCache.values()) texture.dispose();
    }
}

