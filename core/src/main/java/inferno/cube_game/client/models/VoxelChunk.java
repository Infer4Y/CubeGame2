package inferno.cube_game.client.models;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import inferno.cube_game.client.models.blocks.BlockModels;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.levels.chunks.Chunk;

public class VoxelChunk implements RenderableProvider{
    private final Model[] models = new Model[Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE];
    private final Chunk chunkInstance;

    public VoxelChunk(Chunk chunkInstance) {
        this.chunkInstance = chunkInstance;
        for (int blockX = 0; blockX < Chunk.CHUNK_SIZE; blockX++) {
            for (int blockZ = 0; blockZ < Chunk.CHUNK_SIZE; blockZ++) {
                for (int blockY = 0; blockY < Chunk.CHUNK_SIZE; blockY++) {
                    Block block = chunkInstance.getBlock(blockX, blockY, blockZ);

                    if (block == null || block.isAir()) continue;
                    models[blockX * Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE + blockY * Chunk.CHUNK_SIZE + blockZ] = BlockModels.getBlockModel(block);
                }
            }
        }
    }

    @Override
    public void getRenderables(Array<Renderable> array, Pool<Renderable> pool) {
        for (int index = 0; index < models.length; index++) {
            int blockX = index % Chunk.CHUNK_SIZE;
            int blockY = (index / Chunk.CHUNK_SIZE) % Chunk.CHUNK_SIZE;
            int blockZ = index / Chunk.CHUNK_SIZE;

            blockX += Chunk.CHUNK_SIZE * chunkInstance.getChunkX();
            blockY += Chunk.CHUNK_SIZE * chunkInstance.getChunkY();
            blockZ += Chunk.CHUNK_SIZE * chunkInstance.getChunkZ();

            if (models[index] == null) continue;
            int finalBlockX = blockX;
            int finalBlockY = blockY;
            int finalBlockZ = blockZ;
            for (int i = 0; i < models[index].meshes.size; i++) {
                Renderable renderable = pool.obtain();
                renderable.worldTransform.set(new Vector3(finalBlockX, finalBlockY, finalBlockZ), new Quaternion());
                renderable.meshPart.mesh = models[index].meshParts.get(i).mesh;
                renderable.material = models[index].materials.get(i);
                array.add(renderable);
            }

        }
    }
}
