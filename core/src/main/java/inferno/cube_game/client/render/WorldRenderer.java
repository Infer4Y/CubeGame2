package inferno.cube_game.client.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import inferno.cube_game.client.models.GreedyMesher;
import inferno.cube_game.common.levels.World;
import inferno.cube_game.common.levels.chunks.Chunk;

public class WorldRenderer {
    private World world;
    private ModelBatch batch;
    private float cameraYaw = 0f; // Yaw rotation (left-right)
    private float cameraPitch = 0f; // Pitch rotation (up-down)
    private final float mouseSensitivity = 0.2f; // Mouse sensitivity
    private final float maxPitch = 89f; // Prevent camera from flipping
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Vector3 feetPosition; // Player's feet position
    private final float eyeOffset = 1.4f; // Camera height offset from feet (eye level)
    private float lastCull = 0; // Last time chunks were culled
    private Model chunkModel;
    private ModelInstance chunkInstance;
    private GreedyMesher greedyMesher;
    private Environment environment;


    public WorldRenderer(Camera camera, Environment environment) {
        this.world = new World(); // Generate a new world
        this.batch = new ModelBatch();
        Gdx.input.setCursorCatched(true); // Capture the mouse
        feetPosition = camera.position.cpy(); // Set the feet position to the camera position
        this.environment = environment;
        this.greedyMesher = new GreedyMesher();
    }

    public void render(Camera camera, float deltaTime) {
        handleMouseMovement(camera);
        Vector3 movementInput = getPlayerInput();
        updatePlayer(camera, deltaTime, movementInput);
        renderChunks(camera);

        if (System.currentTimeMillis() - lastCull >= 60 * 10000) {
            greedyMesher.cullChunks(camera.position);
            greedyMesher.clearMaterialCache();
            lastCull = System.currentTimeMillis();
        }
    }

    public void update(float deltaTime) {
        world.updateChunks(feetPosition);
    }

    private void handleMouseMovement(Camera camera) {
        // Get mouse delta movements
        // Get mouse delta movements
        if (!Gdx.input.isCursorCatched()) return;  // Skip if cursor is not captured
        float deltaX = -Gdx.input.getDeltaX() * mouseSensitivity; // Mouse movement for yaw
        float deltaY = Gdx.input.getDeltaY() * mouseSensitivity; // Mouse movement for pitch

        // Update yaw (left-right rotation)
        cameraYaw += deltaX;

        // Update pitch (up-down rotation) and clamp it
        cameraPitch -= deltaY;  // Subtracting because moving mouse up should decrease pitch
        cameraPitch = Math.max(-maxPitch, Math.min(maxPitch, cameraPitch));  // Clamp pitch

        // Apply yaw first (rotation around the global Y-axis)
        camera.direction.set(0, 0, -1).rotate(Vector3.Y, cameraYaw);

        // Calculate the camera's right vector for pitch rotation
        Vector3 right = new Vector3(camera.direction).crs(Vector3.Y).nor();  // Cross product to get right vector

        // Apply pitch (rotation around the right vector)
        camera.direction.rotate(right, cameraPitch);

        // Normalize the direction vector to avoid distortion
        camera.direction.nor();
        camera.update();  // Apply the changes to the camera
    }

    private Vector3 getPlayerInput() {
        Vector3 input = new Vector3();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) input.z = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) input.z = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) input.x = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) input.x = 1;
        return input.nor(); // Normalize input for consistent movement speed
    }

    private void updatePlayer(Camera camera, float deltaTime, Vector3 movementInput) {
        //applyGravity(deltaTime);

        // Get movement based on camera direction
        Vector3 forward = new Vector3(camera.direction).nor().scl(movementInput.z);
        Vector3 right = new Vector3(camera.direction).crs(Vector3.Y).nor().scl(movementInput.x);
        Vector3 movement = forward.add(right).scl(50f * deltaTime);


        // Compute new feet position
        feetPosition.add(movement);

        // Set the camera position to feetPosition + eyeOffset
        camera.position.set(feetPosition.x, feetPosition.y + eyeOffset, feetPosition.z);
        camera.update();


        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched()); // Unlock and show cursor
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)){
            if (Gdx.graphics.isFullscreen()){
                Gdx.graphics.setWindowedMode(1280, 720);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }
    }

    private void renderChunks(Camera camera) {
        Vector3 frustumPosition = feetPosition;

        int x = (int) (frustumPosition.x / Chunk.CHUNK_SIZE);
        int y = (int) (frustumPosition.y / Chunk.CHUNK_SIZE);
        int z = (int) (frustumPosition.z / Chunk.CHUNK_SIZE);

        batch.begin(camera);
        // Load nearby chunks
        for (Vector3 loadedChunk : world.getChunkKeysToLoad(x, y, z)) {
            int chunkX = (int) loadedChunk.x;
            int chunkY = (int) loadedChunk.y;
            int chunkZ = (int) loadedChunk.z;

            Chunk chunk = world.getChunk(chunkX, chunkY, chunkZ);


            if (chunk == null) continue;
            if (chunk.onlyAir()) continue;
            if (!checkSurrondingChunksForAir(world, chunk)) continue;

            if (chunkModel == null) {
                ModelBuilder modelBuilder = new ModelBuilder();

                chunkModel = greedyMesher.generateMesh(chunk, modelBuilder);
            }

            chunkInstance = new ModelInstance(chunkModel);

            chunkInstance.transform.setToTranslation(chunk.getChunkX() * Chunk.CHUNK_SIZE, chunk.getChunkY() * Chunk.CHUNK_SIZE, chunk.getChunkZ() * Chunk.CHUNK_SIZE);

            batch.render(chunkInstance, environment);

            chunkModel = null;
            chunkInstance = null;
        }

        batch.end();
    }

    private boolean checkSurrondingChunksForAir(World world, Chunk chunk) {
        int chunkX = chunk.getChunkX();
        int chunkY = chunk.getChunkY();
        int chunkZ = chunk.getChunkZ();

        Chunk top = world.getChunk(chunkX, chunkY+1, chunkZ);
        if (top == null) return true;
        if (!top.hasNoAirInAnyLayer()) return true;

        Chunk bottom = world.getChunk(chunkX, chunkY-1, chunkZ);
        if (bottom == null) return true;
        if (!bottom.hasNoAirInAnyLayer()) return true;

        Chunk left = world.getChunk(chunkX-1, chunkY, chunkZ);
        if (left == null) return true;
        if (!left.hasNoAirInAnyLayer()) return true;

        Chunk right  = world.getChunk(chunkX+1, chunkY, chunkZ);
        if (right == null) return true;
        if (!right.hasNoAirInAnyLayer()) return true;

        Chunk front  = world.getChunk(chunkX, chunkY, chunkZ+1);
        if (front == null) return true;
        if (!front.hasNoAirInAnyLayer()) return true;

        Chunk back  = world.getChunk(chunkX, chunkY, chunkZ-1);
        if (back == null) return true;
        if (!back.hasNoAirInAnyLayer()) return true;

        return false;
    }

    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        // Dispose other resources if necessary
        world.shutdown();
    }
}
