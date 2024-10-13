package inferno.cube_game.client.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import inferno.cube_game.client.sounds.MusicAssets;

import java.util.HashMap;

public class GameAndModMusicLoader {
    public HashMap <String, Music> musicCache; // Cache for music to avoid reloading them every time they are needed
    private Json json; // JSON parser for loading music

    public GameAndModMusicLoader() {
        this.musicCache = new HashMap<>(); // Initialize the music cache
        this.json = new Json(); // Initialize the JSON parser
    }

    // Load music from the assets folder and cache it if it hasn't been loaded yet
    public void loadMusic(String domain) {
        String path = "assets/" + domain + "/music/music.json"; // Construct the path to the music file
        // Load the music from the file
        MusicAssets musicAssets = json.fromJson(MusicAssets.class, path);

        if (musicAssets == null) return;
        if (musicAssets.domainMusic == null) return;
        if (musicAssets.domainMusic.isEmpty()) return;

        for (String music : musicAssets.domainMusic) {
            // Load the music from the file
            Music musicFile = Gdx.audio.newMusic(Gdx.files.internal("assets/" + domain + "/music/" + music));
            musicCache.put(music, musicFile); // Cache the music
        }

    }

    public void dispose(){ // Dispose of all music in the cache
        // Dispose of all music in the cache
    }
}
