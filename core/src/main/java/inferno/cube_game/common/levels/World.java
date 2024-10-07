package inferno.cube_game.common.levels;

import com.badlogic.gdx.math.Vector3;
import inferno.cube_game.common.levels.chunks.Chunk;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.HashSet;

public class World {
    private ConcurrentHashMap<String, Chunk> loadedChunks;
    private final ExecutorService chunkGeneratorExecutor;
    private int chunkLoadRadius = 17; // Number of chunks to load around the player (in all directions)
    private long seed = System.currentTimeMillis(); // Seed for the world generation

    public World() {
        loadedChunks = new ConcurrentHashMap<>();
        chunkGeneratorExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    private String getChunkKey(int chunkX, int chunkY, int chunkZ) {
        return chunkX + "," + chunkY + "," + chunkZ;
    }

    public Chunk getChunk(int chunkX, int chunkY, int chunkZ) {
        String key = getChunkKey(chunkX, chunkY, chunkZ);
        return loadedChunks.get(key);
    }

    public void updateChunks(Vector3 playerPosition) {
        int playerChunkX = (int) (playerPosition.x / Chunk.CHUNK_SIZE);
        int playerChunkY = (int) (playerPosition.y / Chunk.CHUNK_SIZE);
        int playerChunkZ = (int) (playerPosition.z / Chunk.CHUNK_SIZE);

        HashSet<String> chunkKeysToLoad = getChunkKeysToLoad(playerChunkX, playerChunkY, playerChunkZ);

        // Load new chunks asynchronously
        for (String key : chunkKeysToLoad) {
            int chunkX = chunkXFromKey(key);
            int chunkY = chunkYFromKey(key);
            int chunkZ = chunkZFromKey(key);

            // Submit chunk generation task to executor
            chunkGeneratorExecutor.submit(() -> {
                loadedChunks.computeIfAbsent(key, k -> new Chunk(seed, chunkX, chunkY, chunkZ));
            });
        }

        // Unload chunks that are too far from the player
        loadedChunks.keySet().removeIf(key -> {
            int[] coords = getChunkCoordinates(key);
            return Math.abs(coords[0] - playerChunkX) > chunkLoadRadius ||
                Math.abs(coords[1] - playerChunkY) > chunkLoadRadius ||
                Math.abs(coords[2] - playerChunkZ) > chunkLoadRadius;
        });
    }

    public HashSet<String> getChunkKeysToLoad(int playerChunkX, int playerChunkY, int playerChunkZ) {
        HashSet<String> chunkKeysToLoad = new HashSet<>();
        for (int x = playerChunkX - chunkLoadRadius; x <= playerChunkX + chunkLoadRadius; x++) {
            for (int y = playerChunkY - chunkLoadRadius; y <= playerChunkY + chunkLoadRadius; y++) {
                for (int z = playerChunkZ - chunkLoadRadius; z <= playerChunkZ + chunkLoadRadius; z++) {
                    chunkKeysToLoad.add(getChunkKey(x, y, z));
                }
            }
        }
        return chunkKeysToLoad;
    }

    private int chunkXFromKey(String key) {
        return Integer.parseInt(key.split(",")[0]);
    }

    private int chunkYFromKey(String key) {
        return Integer.parseInt(key.split(",")[1]);
    }

    private int chunkZFromKey(String key) {
        return Integer.parseInt(key.split(",")[2]);
    }

    public int[] getChunkCoordinates(String key) {
        String[] parts = key.split(",");
        return new int[]{
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2])
        };
    }

    public void shutdown() {
        chunkGeneratorExecutor.shutdown();
    }
}
