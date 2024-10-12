// ChunkRenderer.java
package inferno.cube_game.client.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import inferno.cube_game.client.models.GreedyMesher;
import inferno.cube_game.common.levels.chunks.Chunk;

public class ChunkRenderer {
    private Environment environment;
    private BoundingBox chunkBoundingBox;
    private Model chunkModel;
    private ModelInstance chunkInstance;
    private GreedyMesher greedyMesher;

    public ChunkRenderer(Camera camera, Environment environment) {
        this.environment = environment;
        this.chunkBoundingBox = new BoundingBox();
        this.greedyMesher = new GreedyMesher();
    }

    public void render(ModelBatch modelBatch, Camera camera, Chunk chunk) {
        if (chunkModel == null) {
            buildChunkModel(chunk);
        }

        modelBatch.begin(camera);
        modelBatch.render(chunkInstance, environment);
        modelBatch.end();

        chunkModel = null;
    }

    private void buildChunkModel(Chunk chunk) {
        ModelBuilder modelBuilder = new ModelBuilder();

        chunkModel = greedyMesher.generateMesh(chunk, modelBuilder);

        chunkInstance = new ModelInstance(chunkModel);
        chunkInstance.transform.setToTranslation(chunk.getChunkX() * Chunk.CHUNK_SIZE, chunk.getChunkY() * Chunk.CHUNK_SIZE, chunk.getChunkZ() * Chunk.CHUNK_SIZE);
    }

    public void dispose() {
        if (chunkModel != null) {
            chunkModel.dispose();
        }
        greedyMesher.dispose();
    }

    public void cullChunks(Vector3 position) {
        greedyMesher.cullChunks(position);
    }

    public void clearMaterialCache() {
        greedyMesher.clearMaterialCache();
    }
}
