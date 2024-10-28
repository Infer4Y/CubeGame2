package inferno.cube_game.common.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.levels.chunks.Chunk;
import inferno.cube_game.common.levels.chunks.ChunkGenerator;
import inferno.cube_game.common.registries.BlockRegistry;

import java.io.*;
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
    private final ConcurrentHashMap<Vector3, Future<Chunk>> loadingChunks; // Track chunks being generated
    private final ExecutorService chunkGeneratorExecutor;
    private int chunkLoadRadius = 7; // Number of chunks to load around the player
    private int chunkLoadVisableRadius = 6; // Number of chunks to load around the player
    private long seed = 0; //(System.currentTimeMillis() + System.nanoTime()) / 2; // World generation seed
    private final ChunkGenerator chunkGenerator = new ChunkGenerator(seed);
    private long lastChunkUnloadTime = System.currentTimeMillis();

    public static final byte[] emptyChunk = new byte[Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE];
    static {
        Arrays.fill(emptyChunk, (byte) 0);
    }

    /**
     * Create a new world
     */
    public World() {
        loadingChunks = new ConcurrentHashMap<>(); // Track chunks being generated by key (X,Y,Z) coordinates of the chunk being generated
        chunkGeneratorExecutor = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors()/4-1, 1)); // Create a thread pool for chunk generation
    }

    /**
     * Get the key for a chunk based on its coordinates
     * @param chunkX the coordinate to chunk in x
     * @param chunkY the coordinate to chunk in y
     * @param chunkZ the coordinate to chunk in y
     * @return Chunk key
     */
    private Vector3 getChunkKey(int chunkX, int chunkY, int chunkZ) {
        return new Vector3(chunkX, chunkY, chunkZ); // Key is formatted as "X,Y,Z"
    }

    /**
     * Get a chunk from the world by its coordinates
     * @param chunkX
     * @param chunkY
     * @param chunkZ
     * @return Chunk object
     */
    public Chunk getChunk(int chunkX, int chunkY, int chunkZ) {
        Vector3 key = getChunkKey(chunkX, chunkY, chunkZ); // Generate key from coordinates
        Future<Chunk> future = loadingChunks.get(key); // Check if the chunk is being generated or already exists

        if (future != null) { // If future exists
            if (!future.isDone()) return null; // Return null if chunk hasn't finished generating
            try {
                return future.get(); // Return chunk if it’s done
            } catch (InterruptedException | ExecutionException e) {
                Gdx.app.error("World", "Error getting chunk", e);
                return null;
            }
        }

        // If chunk isn't found on disk, initiate generation
        Future<Chunk> newChunkFuture = chunkGeneratorExecutor.submit(() -> chunkGenerator.generateChunk(chunkX, chunkY, chunkZ));
        loadingChunks.put(key, newChunkFuture);
        return null; // Chunk is being generated
    }


    /**
     * Update the chunks around the player or passed in coordinates
     * @param playerPosition
     */
    public void updateChunks(Vector3 playerPosition) {
        int playerChunkX = (int) (playerPosition.x / Chunk.CHUNK_SIZE); // Get the player's chunk X coordinate
        int playerChunkY = (int) (playerPosition.y / Chunk.CHUNK_SIZE); // Get the player's chunk Y coordinate
        int playerChunkZ = (int) (playerPosition.z / Chunk.CHUNK_SIZE); // Get the player's chunk Z coordinate

        ArrayList<Vector3> chunkKeysToLoad = getChunksKeysLoadedByWorld(playerChunkX, playerChunkY, playerChunkZ, chunkLoadRadius); // Get the keys of chunks to load around the player

        // Load new chunks asynchronously
        chunkKeysToLoad.parallelStream().forEach(chunkKey -> {
            int chunkX = (int) chunkKey.x; // Get the chunk X coordinate from the key
            int chunkY = (int) chunkKey.y; // Get the chunk Y coordinate from the key
            int chunkZ = (int) chunkKey.z; // Get the chunk Z coordinate from the key


            // Submit chunk generation as Future tasks
            loadingChunks.computeIfAbsent(chunkKey, k -> chunkGeneratorExecutor.submit(() -> chunkGenerator.generateChunk(chunkX, chunkY, chunkZ))); // Generate the chunk and add it to the loadingChunks map

        });

        loadingChunks.values().forEach(chunkFuture -> {
            if (!chunkFuture.isDone()) return;

        });

        if (System.currentTimeMillis() - lastChunkUnloadTime > 60 * 10 * 1000L) {
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

            if (playerChunkX - chunkLoadRadius > entry.getKey().x) return true;
            if (playerChunkX + chunkLoadRadius < entry.getKey().x) return true;
            if (playerChunkY - chunkLoadRadius > entry.getKey().y) return true;
            if (playerChunkY + chunkLoadRadius < entry.getKey().y) return true;
            if (playerChunkZ - chunkLoadRadius > entry.getKey().z) return true;
            if (playerChunkZ + chunkLoadRadius < entry.getKey().z) return true;

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
    public ArrayList<Vector3> getChunkKeysToLoad(int playerChunkX, int playerChunkY, int playerChunkZ) {
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
    private ArrayList<Vector3> getChunksKeysLoadedByWorld(int playerChunkX, int playerChunkY, int playerChunkZ, int chunkLoadRadius) {
        int diameter = chunkLoadRadius * 2; // Diameter of the chunk load radius
        ArrayList<Vector3> chunkKeysToLoad = new ArrayList<>(diameter * diameter * diameter); // List of chunk keys to load
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
        //loadingChunks.forEach((key, future) -> {saveChunk(future.resultNow());});
    }
}
