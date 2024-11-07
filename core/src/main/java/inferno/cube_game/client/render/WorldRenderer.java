package inferno.cube_game.client.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
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
    private ModelInstance chunkInstance;
    private GreedyMesher greedyMesher;
    private Environment environment;

    ModelCache chunkModelCache;

    Model globalModel;

    ModelInstance instance;

    public WorldRenderer(Camera camera, Environment environment) {
        this.world = new World(); // Generate a new world
        this.batch = //new ModelBatch(new DefaultShaderProvider() {
        //    @Override
        //    protected Shader createShader(Renderable renderable) {
        //        return new WireframeShader(renderable, config);
        //    }
        //});
            new ModelBatch();
        Gdx.input.setCursorCatched(true); // Capture the mouse
        feetPosition = camera.position.cpy(); // Set the feet position to the camera position
        this.environment = environment;
        this.greedyMesher = new GreedyMesher();
        chunkModelCache = new ModelCache();
        globalModel = new Model();
        instance = new ModelInstance(globalModel);
    }

    public void render(Camera camera, float deltaTime) {
        handleMouseMovement(camera);
        Vector3 movementInput = getPlayerInput();
        updatePlayer(camera, deltaTime, movementInput);
        renderChunks(camera);

        if (System.currentTimeMillis() - lastCull >= 60 * 10000) {
            //greedyMesher.cullChunks(camera.position);
            //greedyMesher.clearMaterialCache();
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

    }

    private void renderChunks(Camera camera) {
        Vector3 frustumPosition = feetPosition;

        Renderable renderable = new Renderable();

        instance.nodes.clear();
        instance.model.meshes.clear();
        instance.model.meshParts.clear();
        instance.materials.clear();

        int x = (int) (frustumPosition.x / Chunk.CHUNK_SIZE);
        int y = (int) (frustumPosition.y / Chunk.CHUNK_SIZE);
        int z = (int) (frustumPosition.z / Chunk.CHUNK_SIZE);

        // Load nearby chunks
        world.getChunkKeysToLoad(x, y, z).forEach(loadedChunk -> {
            int chunkX = (int) loadedChunk.x;
            int chunkY = (int) loadedChunk.y;
            int chunkZ = (int) loadedChunk.z;

            Chunk chunk = world.getChunk(chunkX, chunkY, chunkZ);

            if (chunk == null) return;
            if (chunk.onlyAir()) return;
            if (!checkSurrondingChunksForAir(world, chunk)) return;

            //batch.render(new VoxelChunk(chunk));
            Model model = greedyMesher.generateMesh(chunk);
            if (model == null) return;

            instance.nodes.addAll(model.nodes);
            instance.model.meshes.addAll(model.meshes);
            instance.model.meshParts.addAll(model.meshParts);
            instance.materials.addAll(model.materials);
        });

        batch.begin(camera);

        batch.render(instance, environment);

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
        return !back.hasNoAirInAnyLayer();
    }

    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        chunkModelCache.dispose();
        globalModel.dispose();
        // Dispose other resources if necessary
        world.shutdown();
    }
}
