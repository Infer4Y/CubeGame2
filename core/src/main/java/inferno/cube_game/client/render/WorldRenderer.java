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
    private final float eyeOffset = 1.6f; // Camera height offset from feet (eye level)
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
        applyGravity(deltaTime);

        // Get movement based on camera direction
        Vector3 forward = new Vector3(camera.direction).nor().scl(movementInput.z);
        Vector3 right = new Vector3(camera.direction).crs(Vector3.Y).nor().scl(movementInput.x);
        Vector3 movement = forward.add(right).scl(50f * deltaTime);

        // Compute new feet position
        Vector3 newPosition = feetPosition.cpy().add(movement);

        // Bounding box at feetPosition
        BoundingBox playerBB = getPlayerBoundingBox(feetPosition);

        // Collision handling
        if (checkCollisionWithWorld(newPosition, playerBB)) {
            newPosition = resolveCollisionWithWorld(newPosition, playerBB);
        }

        // Update the player's feet position
        feetPosition.set(newPosition);

        // Set the camera position to feetPosition + eyeOffset
        camera.position.set(feetPosition.x, feetPosition.y + eyeOffset-.2f, feetPosition.z);
        camera.update();

        // Jumping logic, ground check, etc.

        if (checkCollisionWithWorld(feetPosition, playerBB)) {
            feetPosition.y = getGroundHeight(feetPosition); // Snap feet to the ground
            velocity.y = 0;
            isGrounded = true;
        } else {
            isGrounded = false;
        }

        handleJumping();

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched()); // Unlock and show cursor
        }
    }

    private void applyGravity(float deltaTime) {
        if (!isGrounded) {
            velocity.add(gravity.cpy().scl(deltaTime)); // Apply gravity
        }
    }

    private void handleJumping() {
        if (isJumpKeyPressed() && isGrounded) {
            velocity.y = jumpForce; // Apply jump force
            isGrounded = false; // Not grounded after jumping
        }
    }

    private BoundingBox getPlayerBoundingBox(Vector3 camera) {
        Vector3 min = camera.cpy().sub(0.5f, 0.0f, 0.5f); // Bottom corner
        Vector3 max = camera.cpy().add(0.5f, playerHeight, 0.5f); // Top corner
        return new BoundingBox(min, max);
    }

    private boolean isJumpKeyPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.SPACE);
    }

    private float getGroundHeight(Vector3 playerPosition) {
        int chunkX = (int) (playerPosition.x / Chunk.CHUNK_SIZE);
        int chunkY = (int) (playerPosition.y / Chunk.CHUNK_SIZE);
        int chunkZ = (int) (playerPosition.z / Chunk.CHUNK_SIZE);

        for (int cy = chunkY; cy >= 0; cy--) { // Check downwards
            Chunk chunk = world.getChunk(chunkX, cy, chunkZ);
            if (chunk == null) continue; // Skip if chunk is not loaded

            for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                    for (int y = Chunk.CHUNK_SIZE - 1; y >= 0; y--) { // Check from top to bottom
                        Block block = chunk.getBlock(x, y, z);
                        if (block != null && block.isSolid()) {
                            return (cy * Chunk.CHUNK_SIZE + y) + 1; // +1 to get the top of the block
                        }
                    }
                }
            }
        }

        return 0; // Default height if no ground found
    }

    public boolean checkCollisionWithWorld(Vector3 playerPosition, BoundingBox playerBB) {
        int chunkX = (int) (playerPosition.x / Chunk.CHUNK_SIZE);
        int chunkY = (int) (playerPosition.y / Chunk.CHUNK_SIZE);
        int chunkZ = (int) (playerPosition.z / Chunk.CHUNK_SIZE);

        boolean collisionDetected = false;

        for (int cx = chunkX - 1; cx <= chunkX + 1; cx++) {
            for (int cy = chunkY - 1; cy <= chunkY + 1; cy++) {
                for (int cz = chunkZ - 1; cz <= chunkZ + 1; cz++) {
                    Chunk chunk = world.getChunk(cx, cy, cz);
                    if (chunk == null) continue; // Skip if chunk is not loaded

                    collisionDetected |= checkChunkCollision(chunk, playerBB);
                }
            }
        }

        return collisionDetected;
    }

    private boolean checkChunkCollision(Chunk chunk, BoundingBox playerBB) {
        boolean collisionDetected = false;

        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block != null && block.isSolid()) {
                        BoundingBox blockBB = new BoundingBox(
                            new Vector3(
                                chunk.getChunkX() * Chunk.CHUNK_SIZE + x,
                                chunk.getChunkY() * Chunk.CHUNK_SIZE + y,
                                chunk.getChunkZ() * Chunk.CHUNK_SIZE + z
                            ),
                            new Vector3(
                                chunk.getChunkX() * Chunk.CHUNK_SIZE + x + 1,
                                chunk.getChunkY() * Chunk.CHUNK_SIZE + y + 1,
                                chunk.getChunkZ() * Chunk.CHUNK_SIZE + z + 1
                            )
                        );

                        if (blockBB.intersects(playerBB)) {
                            collisionDetected = true;
                            break; // Exit inner loops on collision
                        }
                    }
                }
            }
        }

        return collisionDetected;
    }

    private Vector3 resolveCollisionWithWorld(Vector3 newPosition, BoundingBox playerBB) {
        for (int cx = (int) (newPosition.x / Chunk.CHUNK_SIZE) - 1; cx <= (int) (newPosition.x / Chunk.CHUNK_SIZE) + 1; cx++) {
            for (int cy = (int) (newPosition.y / Chunk.CHUNK_SIZE) - 1; cy <= (int) (newPosition.y / Chunk.CHUNK_SIZE) + 1; cy++) {
                for (int cz = (int) (newPosition.z / Chunk.CHUNK_SIZE) - 1; cz <= (int) (newPosition.z / Chunk.CHUNK_SIZE) + 1; cz++) {
                    Chunk chunk = world.getChunk(cx, cy, cz);
                    if (chunk == null) continue;

                    for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
                        for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
                            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                                Block block = chunk.getBlock(x, y, z);
                                if (!block.isAir()) {
                                    BoundingBox blockBB = new BoundingBox(
                                        new Vector3(chunk.getChunkX() * Chunk.CHUNK_SIZE + x, chunk.getChunkY() * Chunk.CHUNK_SIZE + y, chunk.getChunkZ() * Chunk.CHUNK_SIZE + z),
                                        new Vector3(chunk.getChunkX() * Chunk.CHUNK_SIZE + x + 1, chunk.getChunkY() * Chunk.CHUNK_SIZE + y + 1, chunk.getChunkZ() * Chunk.CHUNK_SIZE + z + 1)
                                    );

                                    if (blockBB.intersects(playerBB)) {
                                        Vector3 collisionNormal = determineCollisionNormal(playerBB, blockBB);
                                        float penetrationDepth = calculatePenetrationDepth(playerBB, blockBB, collisionNormal);
                                        resolveCollision(newPosition, playerBB, collisionNormal, penetrationDepth); // Adjust player position
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return newPosition;
    }


    private void resolveCollision(Vector3 newPosition, BoundingBox playerBB, Vector3 collisionNormal, float penetrationDepth) {
        playerBB.min.add(collisionNormal.scl(penetrationDepth));
        playerBB.max.add(collisionNormal.scl(penetrationDepth));

        if (collisionNormal.y > 0) {
            velocity.y = 0; // Reset vertical velocity
            isGrounded = true; // Player is on the ground
        } else {
            isGrounded = false; // Player is in the air
        }
    }

    private float calculatePenetrationDepth(BoundingBox playerBB, BoundingBox blockBB, Vector3 collisionNormal) {
        Vector3 playerSize = new Vector3(playerBB.getDimensions(new Vector3()));
        Vector3 blockSize = new Vector3(blockBB.getDimensions(new Vector3()));

        float depthX = (collisionNormal.x != 0) ?
            Math.abs((blockBB.min.x - playerBB.max.x) * collisionNormal.x) :
            Math.abs((blockBB.max.x - playerBB.min.x) * collisionNormal.x);

        float depthY = (collisionNormal.y != 0) ?
            Math.abs((blockBB.min.y - playerBB.max.y) * collisionNormal.y) :
            Math.abs((blockBB.max.y - playerBB.min.y) * collisionNormal.y);

        float depthZ = (collisionNormal.z != 0) ?
            Math.abs((blockBB.min.z - playerBB.max.z) * collisionNormal.z) :
            Math.abs((blockBB.max.z - playerBB.min.z) * collisionNormal.z);

        return Math.min(depthX, Math.min(depthY, depthZ)); // Return the smallest depth to resolve
    }

    private Vector3 determineCollisionNormal(BoundingBox playerBB, BoundingBox blockBB) {
        Vector3 playerCenter = playerBB.getCenter(new Vector3());
        Vector3 blockCenter = blockBB.getCenter(new Vector3());
        Vector3 collisionNormal = new Vector3();

        collisionNormal.set(
            playerCenter.x - blockCenter.x,
            playerCenter.y - blockCenter.y,
            playerCenter.z - blockCenter.z
        ).nor(); // Normalize to get the direction

        return collisionNormal; // Return the collision normal
    }

    private void renderChunks(Camera camera) {
        Vector3 frustumPosition = feetPosition;
        int centerChunkSize = Chunk.CHUNK_SIZE / 2;
        int x = (int) (frustumPosition.x / Chunk.CHUNK_SIZE);
        int y = (int) (frustumPosition.y / Chunk.CHUNK_SIZE);
        int z = (int) (frustumPosition.z / Chunk.CHUNK_SIZE);

        // List to store chunks and their distances
        List<Pair<Chunk, Float>> chunkDistances = new ArrayList<>();

        // Load nearby chunks
        for (String loadedChunk : world.getChunkKeysToLoad(x, y, z)) {
            int[] coords = world.getChunkCoordinates(loadedChunk);
            Chunk chunk = world.getChunk(coords[0], coords[1], coords[2]);
            if (chunk == null) continue;

            // Calculate the distance to the chunk
            float distance = frustumPosition.dst(new Vector3(
                (coords[0] + centerChunkSize) * Chunk.CHUNK_SIZE,
                (coords[1] + centerChunkSize) * Chunk.CHUNK_SIZE,
                (coords[2] + centerChunkSize) * Chunk.CHUNK_SIZE
            ));
            chunkDistances.add(new Pair<>(chunk, distance));
        }

        // Sort chunks by distance
        chunkDistances.sort(Comparator.comparing(Pair::getValue));

        // Define a box size for visibility check
        float boxSize = 12 * Chunk.CHUNK_SIZE; // Adjust this value as needed

        // Render chunks in order of distance
        for (Pair<Chunk, Float> pair : chunkDistances) {
            Chunk chunk = pair.getKey();
            Vector3 chunkPosition = new Vector3(
                chunk.getChunkX() * Chunk.CHUNK_SIZE,
                chunk.getChunkY() * Chunk.CHUNK_SIZE,
                chunk.getChunkZ() * Chunk.CHUNK_SIZE
            );

            // Check if the chunk is within the visibility box
            if (Math.abs(chunkPosition.x - frustumPosition.x) <= boxSize &&
                Math.abs(chunkPosition.y - frustumPosition.y) <= boxSize &&
                Math.abs(chunkPosition.z - frustumPosition.z) <= boxSize) {
                chunkRenderer.render(batch, camera, chunk);
            }
        }
    }

    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        // Dispose other resources if necessary
        world.shutdown();
    }
}
