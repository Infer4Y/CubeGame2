package inferno.cube_game.client.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import inferno.cube_game.Pair;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.levels.World;
import inferno.cube_game.common.levels.chunks.Chunk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WorldRenderer {
    private World world;
    private ChunkRenderer chunkRenderer;
    private ModelBatch batch;
    private Vector3 velocity = new Vector3(); // Player velocity
    private final Vector3 gravity = new Vector3(0, -9.8f, 0); // Gravity constant
    private float jumpForce = 15f; // Jump strength
    private boolean isGrounded = false; // Check if player is on the ground
    private float cameraYaw = 0f; // Yaw rotation (left-right)
    private float cameraPitch = 0f; // Pitch rotation (up-down)
    private final float mouseSensitivity = 0.2f; // Mouse sensitivity
    private final float maxPitch = 89f; // Prevent camera from flipping
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Vector3 feetPosition = new Vector3(); // Player's feet position
    private final float playerHeight = 1.8f; // Height of the player (camera offset)
    private final float eyeOffset = 1.4f; // Camera height offset from feet (eye level)
    private float lastCull = 0; // Last time chunks were culled


    public WorldRenderer(Camera camera, Environment environment) {
        this.world = new World(); // Generate a new world
        this.chunkRenderer = new ChunkRenderer(camera, environment);
        this.batch = new ModelBatch();
        Gdx.input.setCursorCatched(true); // Capture the mouse
    }

    public void render(Camera camera, float deltaTime) {
        handleMouseMovement(camera);
        Vector3 movementInput = getPlayerInput();
        updatePlayer(camera, deltaTime, movementInput);
        world.updateChunks(camera.position);
        renderChunks(camera);

        if (System.currentTimeMillis() - lastCull >= 10000) {
            chunkRenderer.cullChunks(camera.position);
            lastCull = System.currentTimeMillis();
        }
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

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched()); // Unlock and show cursor
        }
    }

    private void renderChunks(Camera camera) {
        Vector3 frustumPosition = feetPosition;

        int x = (int) (frustumPosition.x / Chunk.CHUNK_SIZE);
        int y = (int) (frustumPosition.y / Chunk.CHUNK_SIZE);
        int z = (int) (frustumPosition.z / Chunk.CHUNK_SIZE);

        // Load nearby chunks
        for (String loadedChunk : world.getChunkKeysToLoad(x, y, z)) {
            int[] coords = world.getChunkCoordinates(loadedChunk);
            int chunkX = coords[0];
            int chunkY = coords[1];
            int chunkZ = coords[2];
            Chunk chunk = world.getChunk(chunkX, chunkY, chunkZ);
            if (chunk == null) continue;
            chunkRenderer.render(batch, camera, chunk);
        }
    }

    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        // Dispose other resources if necessary
        world.shutdown();
    }
}
