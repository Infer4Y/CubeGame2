package inferno.cube_game.client.models.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;

public class TextureLoader {
    private HashMap<String, Texture> textureCache = new HashMap<>();
    private Json json = new Json();

    public Texture loadTexture(String texturePath) {
        String[] arrayS = texturePath.split(":");
        if (!textureCache.containsKey(texturePath)) {
            Texture texture = new Texture(Gdx.files.internal("assets/" + arrayS[0] + "/textures/" + arrayS[1]));
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

