package inferno.cube_game.common.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import inferno.cube_game.common.levels.chunks.Chunk;
import inferno.cube_game.common.levels.chunks.ChunkGenerator;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * World class that manages chunks and chunk generation
 * @see Chunk
 * @see ChunkGenerator
 * @author inferno4you
 */
public class World {
    private ConcurrentHashMap<String, Future<Chunk>> loadingChunks; // Track chunks being generated
    private final ExecutorService chunkGeneratorExecutor;
    private int chunkLoadRadius = 24; // Number of chunks to load around the player
    private int chunkLoadVisableRadius = 10; // Number of chunks to load around the player
    private long seed = (System.currentTimeMillis() + System.nanoTime()) / 2; // World generation seed
    private ChunkGenerator chunkGenerator = new ChunkGenerator(seed);
    private long lastChunkUnloadTime = System.currentTimeMillis();

    /**
     * Create a new world
     */
    public World() {
        loadingChunks = new ConcurrentHashMap<>(); // Track chunks being generated by key (X,Y,Z) coordinates of the chunk being generated
        chunkGeneratorExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); // Create a thread pool for chunk generation
    }

    /**
     * Get the key for a chunk based on its coordinates
     * @param chunkX
     * @param chunkY
     * @param chunkZ
     * @return Chunk key
     */
    private String getChunkKey(int chunkX, int chunkY, int chunkZ) {
        return chunkX + "," + chunkY + "," + chunkZ; // Key is formatted as "X,Y,Z"
    }

    /**
     * Get a chunk from the world by its coordinates
     * @param chunkX
     * @param chunkY
     * @param chunkZ
     * @return Chunk object
     */
    public Chunk getChunk(int chunkX, int chunkY, int chunkZ) {
        String key = getChunkKey(chunkX, chunkY, chunkZ); // Generate key from coordinates
        Future<Chunk> future = loadingChunks.get(key); // Get the chunk by key
        if (future == null) return null; // Return null if chunk doesn't exist

        if (!future.isDone()) return null; // Return null if chunk hasn't been generated yet

        try {
            return future.get(); // Return the chunk if it's done being generated
        } catch (InterruptedException | ExecutionException e) {
            Gdx.app.error("World", "Error getting chunk", e);
        }

        return null; // Return null if chunk hasn't been generated yet
    }

    /**
     * Update the chunks around the player or passed in coordinates
     * @param playerPosition
     */
    public void updateChunks(Vector3 playerPosition) {
        int playerChunkX = (int) (playerPosition.x / Chunk.CHUNK_SIZE); // Get the player's chunk X coordinate
        int playerChunkY = (int) (playerPosition.y / Chunk.CHUNK_SIZE); // Get the player's chunk Y coordinate
        int playerChunkZ = (int) (playerPosition.z / Chunk.CHUNK_SIZE); // Get the player's chunk Z coordinate

        ArrayList<String> chunkKeysToLoad = getChunksKeysLoadedByWorld(playerChunkX, playerChunkY, playerChunkZ, chunkLoadRadius); // Get the keys of chunks to load around the player

        // Load new chunks asynchronously
        for (String key : chunkKeysToLoad) {
            int chunkX = chunkXFromKey(key); // Get the chunk X coordinate from the key
            int chunkY = chunkYFromKey(key); // Get the chunk Y coordinate from the key
            int chunkZ = chunkZFromKey(key); // Get the chunk Z coordinate from the key

            // Submit chunk generation as Future tasks
            loadingChunks.computeIfAbsent(key, k -> chunkGeneratorExecutor.submit(() -> chunkGenerator.generateChunk(chunkX, chunkY, chunkZ))); // Generate the chunk and add it to the loadingChunks map
        }

        if (System.currentTimeMillis() - lastChunkUnloadTime > 1000 * 60) {
            cullTooFarChunks(playerChunkX, playerChunkY, playerChunkZ); // Unload chunks that are too far from the player
            lastChunkUnloadTime = System.currentTimeMillis();
        }
    }

    /**
     * Unload chunks that are too far from the player
     * @param playerChunkX
     * @param playerChunkY
     * @param playerChunkZ
     */
    private void cullTooFarChunks(int playerChunkX, int playerChunkY, int playerChunkZ) {
        // Unload chunks that are too far from the player
        loadingChunks.entrySet().removeIf(entry -> {

            if (playerChunkX - chunkLoadRadius > chunkXFromKey(entry.getKey())) return true;
            if (playerChunkX + chunkLoadRadius < chunkXFromKey(entry.getKey())) return true;
            if (playerChunkY - chunkLoadRadius > chunkYFromKey(entry.getKey())) return true;
            if (playerChunkY + chunkLoadRadius < chunkYFromKey(entry.getKey())) return true;
            if (playerChunkZ - chunkLoadRadius > chunkZFromKey(entry.getKey())) return true;
            if (playerChunkZ + chunkLoadRadius < chunkZFromKey(entry.getKey())) return true;

            return false;
        });
    }

    /**
     * Get the keys of chunks that should be loaded around the player within the @chunkLoadVisableRadius variable
     * @param playerChunkX
     * @param playerChunkY
     * @param playerChunkZ
     * @return Set of chunk keys to load
     */
    public ArrayList<String> getChunkKeysToLoad(int playerChunkX, int playerChunkY, int playerChunkZ) {
        return getChunksKeysLoadedByWorld(playerChunkX, playerChunkY, playerChunkZ, chunkLoadVisableRadius);
    }

    /**
     * Get the keys of chunks that should be loaded around the player or passed in coordinates within a certain radius
     * @param playerChunkX
     * @param playerChunkY
     * @param playerChunkZ
     * @param chunkLoadRadius
     * @return Set of chunk keys to load
     */
    private ArrayList<String> getChunksKeysLoadedByWorld(int playerChunkX, int playerChunkY, int playerChunkZ, int chunkLoadRadius) {
        int diameter = chunkLoadRadius * 2; // Diameter of the chunk load radius
        ArrayList<String> chunkKeysToLoad = new ArrayList<>(diameter * diameter * diameter); // List of chunk keys to load
        // Iterate over the range of chunks to load around the player
        IntStream.range(0, diameter * diameter * diameter).forEach(index -> {
            int z = index % diameter;
            int y = (index / diameter) % diameter;
            int x = index / (diameter * diameter);

            // Get the chunk coordinates around the player
            int chunkX = (playerChunkX - chunkLoadRadius) + x;
            int chunkY = (playerChunkY - chunkLoadRadius) + y;
            int chunkZ = (playerChunkZ - chunkLoadRadius) + z;

            // Add the chunk key to the list of chunks to load
            chunkKeysToLoad.add(getChunkKey(chunkX, chunkY, chunkZ));
        });
        return chunkKeysToLoad;
    }

    /**
     * Get the X coordinate of a chunk from its key. The key is formatted as "X,Y,Z".
     * This method splits the key by commas and returns the X coordinate.
     * @param key
     * @return X coordinate of the chunk
     */
    private int chunkXFromKey(String key) {
        return Integer.parseInt(key.split(",")[0]);
    }

    /**
     * Get the Y coordinate of a chunk from its key. The key is formatted as "X,Y,Z".
     * This method splits the key by commas and returns the Y coordinate.
     * @param key
     * @return Y coordinate of the chunk
     */
    private int chunkYFromKey(String key) {
        return Integer.parseInt(key.split(",")[1]);
    }

    /**
     * Get the Z coordinate of a chunk from its key. The key is formatted as "X,Y,Z".
     * This method splits the key by commas and returns the Z coordinate.
     * @param key
     * @return Z coordinate of the chunk
     */
    private int chunkZFromKey(String key) {
        return Integer.parseInt(key.split(",")[2]);
    }

    /**
     * Get the X, Y, and Z coordinates of a chunk from its key. The key is formatted as "X,Y,Z".
     * This method splits the key by commas and returns the X, Y, and Z coordinates.
     * @param key
     * @return Array containing the X, Y, and Z coordinates of the chunk
     */
    public int[] getChunkCoordinates(String key) {
        String[] parts = key.split(",");
        return new int[]{
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2])
        };
    }

    /**
     * Shutdown the chunk generator executor service
     */
    public void shutdown() {
        chunkGeneratorExecutor.shutdown();
    }
}
