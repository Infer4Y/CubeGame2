package inferno.cube_game.client.models;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import inferno.cube_game.Main;
import inferno.cube_game.Pair;
import inferno.cube_game.client.models.blocks.BlockModel;
import inferno.cube_game.client.models.blocks.BlockModel.Element;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.levels.chunks.Chunk;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GreedyMesher {
    private final Map<String, Pair<Block, Material>> materialCache = new ConcurrentHashMap<>();
    private final Map<String, Pair<Chunk, Model>> modelCache = new ConcurrentHashMap<>();


    private String getChunkKey(Chunk chunk) {
        return chunk.getChunkX() + "," + chunk.getChunkY() + "," + chunk.getChunkZ();
    }

    public Model generateMesh(Chunk chunk, ModelBuilder modelBuilder) {
        String chunkKey = getChunkKey(chunk);

        // Check if the model is already cached
        if (modelCache.containsKey(chunkKey)) {
            if (getFromModelCache(chunk, chunkKey)) return modelCache.get(chunkKey).getValue(); // Return cached model
        }

        modelBuilder.begin();
        Map<Material, MeshPartBuilder> faceBatches = new HashMap<>();

        int chunkSize = Chunk.CHUNK_SIZE;
        for (int blockPosX = 0; blockPosX < chunkSize; blockPosX++) {
            for (int blockPosY = 0; blockPosY < chunkSize; blockPosY++) {
                for (int blockPosZ = 0; blockPosZ < chunkSize; blockPosZ++) {
                    Block block = chunk.getBlock(blockPosX, blockPosY, blockPosZ);

                    // Block-level culling: skip if the block is completely surrounded by solid blocks
                    if (canCullBlock(chunk, blockPosX, blockPosY, blockPosZ)) continue; // Skip this block
                    if (block.isAir()) continue; // Skip air blocks
                    if (!block.isSolid()) continue; // Skip non-solid blocks

                    BlockModel blockModel = Main.blockModelOven.createOrGetBlockModel(block);

                    if (blockModel == null) continue;
                    if (blockModel.textures.isEmpty()) continue;
                    if (blockModel.elements.isEmpty()) continue;

                    for (Element element : blockModel.elements) {
                        final var faces = element.faces.entrySet();
                        for (var face : faces) {
                            makeMeshFace(chunk, modelBuilder, element, face,
                                blockPosX, blockPosY, blockPosZ, blockModel,
                                block, faceBatches);
                        }
                    }
                }
            }
        }

        Model model = modelBuilder.end();
        modelCache.put(chunkKey, new Pair<>(chunk, model)); // Cache the generated model
        return model;
    }

    private boolean getFromModelCache(Chunk chunk, String chunkKey) {
        if (modelCache.get(chunkKey).getKey() != chunk) {
            modelCache.get(chunkKey).getValue().dispose(); // Dispose of the old model
            return false;
        }
        return true;
    }

    private void makeMeshFace(Chunk chunk, ModelBuilder modelBuilder, Element element, Map.Entry<String, String> face, int blockPosX, int blockPosY, int blockPosZ, BlockModel blockModel, Block block, Map<Material, MeshPartBuilder> faceBatches) {
        if (!isFaceVisible(chunk, blockPosX, blockPosY, blockPosZ, face.getKey())) return;
        String texturePath = blockModel.textures.get(face.getValue());

        // Material caching
        String materialKey = block.getRegistryName() + face.getKey() + texturePath;
        Material material = materialCache.computeIfAbsent(materialKey, key ->
            new Pair<>(block,new Material(TextureAttribute.createDiffuse(Main.textureLoader.loadTexture(texturePath))))
        ).getValue();


        // Out of the entire block, just the element's dimensions
        float elementWidth = element.to.x - element.from.x;
        float elementHeight = element.to.y - element.from.y;
        float elementDepth = element.to.z - element.from.z;

        // Get the position based on the element's dimensions and chunk position
        float chunkPosX = element.from.x + blockPosX;
        float chunkPosY = element.from.y + blockPosY;
        float chunkPosZ = element.from.z + blockPosZ;

        // Create the face based on its direction and dynamic dimensions
        try {
            makeFaceFromDirection(modelBuilder, face, faceBatches, material, blockPosX, blockPosY, blockPosZ, chunkPosX, elementWidth, chunkPosY, elementHeight, chunkPosZ, elementDepth);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void makeFaceFromDirection(ModelBuilder modelBuilder, Map.Entry<String, String> face,
                                              Map<Material, MeshPartBuilder> faceBatches, Material material,
                                              int blockPosX, int blockPosY, int blockPosZ,
                                              float chunkPosX, float chunkPosY, float chunkPosZ,
                                              float elementWidth, float elementHeight, float elementDepth) throws Exception {
        switch (face.getKey()) {
            case "up":
                getMeshPartBuilder(modelBuilder, face, faceBatches, material, blockPosX, blockPosY, blockPosZ).rect(
                    chunkPosX - elementWidth / 2, chunkPosY + elementHeight / 2, chunkPosZ + elementDepth / 2,  // Top-left
                    chunkPosX + elementWidth / 2, chunkPosY + elementHeight / 2, chunkPosZ + elementDepth / 2,  // Top-right
                    chunkPosX + elementWidth / 2, chunkPosY + elementHeight / 2, chunkPosZ - elementDepth / 2,  // Bottom-right
                    chunkPosX - elementWidth / 2, chunkPosY + elementHeight / 2, chunkPosZ - elementDepth / 2,  // Bottom-left
                    0, 1, 0 // Normal (up)
                );
                return;
            case "down":
                getMeshPartBuilder(modelBuilder, face, faceBatches, material, blockPosX, blockPosY, blockPosZ).rect(
                    chunkPosX + elementWidth / 2, chunkPosY - elementHeight / 2, chunkPosZ - elementDepth / 2,  // Top-left
                    chunkPosX + elementWidth / 2, chunkPosY - elementHeight / 2, chunkPosZ + elementDepth / 2,  // Top-right
                    chunkPosX - elementWidth / 2, chunkPosY - elementHeight / 2, chunkPosZ + elementDepth / 2,  // Bottom-right
                    chunkPosX - elementWidth / 2, chunkPosY - elementHeight / 2, chunkPosZ - elementDepth / 2,  // Bottom-left
                    0, -1, 0 // Normal (down)
                );
                return;
            }
            // North face
            case "north":
                getMeshPartBuilder(modelBuilder, face, faceBatches, material, blockPosX, blockPosY, blockPosZ).rect(
                    chunkPosX + elementWidth / 2, chunkPosY - elementHeight / 2, chunkPosZ - elementDepth / 2,  // Bottom-right
                    chunkPosX - elementWidth / 2, chunkPosY - elementHeight / 2, chunkPosZ - elementDepth / 2,  // Bottom-left
                    chunkPosX - elementWidth / 2, chunkPosY + elementHeight / 2, chunkPosZ - elementDepth / 2,  // Top-left
                    chunkPosX + elementWidth / 2, chunkPosY + elementHeight / 2, chunkPosZ - elementDepth / 2,  // Top-right
                    0, 0, -1 // Normal (north)
                );
                return;
            }
            // South face
            case "south":
                getMeshPartBuilder(modelBuilder, face, faceBatches, material, blockPosX, blockPosY, blockPosZ).rect(
                    chunkPosX - elementWidth / 2, chunkPosY - elementHeight / 2, chunkPosZ + elementDepth / 2,  // Bottom-left
                    chunkPosX + elementWidth / 2, chunkPosY - elementHeight / 2, chunkPosZ + elementDepth / 2,  // Bottom-right
                    chunkPosX + elementWidth / 2, chunkPosY + elementHeight / 2, chunkPosZ + elementDepth / 2,  // Top-right
                    chunkPosX - elementWidth / 2, chunkPosY + elementHeight / 2, chunkPosZ + elementDepth / 2,  // Top-left
                    0, 0, 1 // Normal (south)
                );
                return;
            case "west":
                getMeshPartBuilder(modelBuilder, face, faceBatches, material, blockPosX, blockPosY, blockPosZ).rect(
                    chunkPosX - elementWidth / 2, chunkPosY - elementHeight / 2, chunkPosZ - elementDepth / 2,  // Bottom-right
                    chunkPosX - elementWidth / 2, chunkPosY - elementHeight / 2, chunkPosZ + elementDepth / 2,  // Bottom-left
                    chunkPosX - elementWidth / 2, chunkPosY + elementHeight / 2, chunkPosZ + elementDepth / 2,  // Top-left
                    chunkPosX - elementWidth / 2, chunkPosY + elementHeight / 2, chunkPosZ - elementDepth / 2,  // Top-right
                    -1, 0, 0 // Normal (west)
                );
                return;
            case "east":
                getMeshPartBuilder(modelBuilder, face, faceBatches, material, blockPosX, blockPosY, blockPosZ).rect(
                    chunkPosX + elementWidth / 2, chunkPosY - elementHeight / 2, chunkPosZ + elementDepth / 2,  // bottom-left
                    chunkPosX + elementWidth / 2, chunkPosY - elementHeight / 2, chunkPosZ - elementDepth / 2,  // bottom-right
                    chunkPosX + elementWidth / 2, chunkPosY + elementHeight / 2, chunkPosZ - elementDepth / 2,  // top-right
                    chunkPosX + elementWidth / 2, chunkPosY + elementHeight / 2, chunkPosZ + elementDepth / 2,  // top-left
                    1, 0, 0 // Normal (east)
                );
                return;
            default:
                throw new Exception("Unknown face: " + face.getKey());
        }
        System.out.println("Unknown face: " + faceEntry.getKey());
    }

    private static MeshPartBuilder getMeshPartBuilder(ModelBuilder modelBuilder, Map.Entry<String, String> faceEntry, Map<Material, MeshPartBuilder> faceBatches, Material material, int finalX, int finalY, int finalZ) {
        return faceBatches.computeIfAbsent(material, mat ->
            modelBuilder.part("batch_" + "x_"+ finalX + "y_" + finalY + "z_" + finalZ + "face_" + faceEntry.getKey().toUpperCase(), GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                mat));
    }

    private boolean canCullBlock(Chunk chunk, int x, int y, int z) {
        // Check all six neighbors
        return !chunk.getBlock(x - 1, y, z).isAir() &&
            !chunk.getBlock(x + 1, y, z).isAir() &&
            !chunk.getBlock(x, y - 1, z).isAir() &&
            !chunk.getBlock(x, y + 1, z).isAir() &&
            !chunk.getBlock(x, y, z - 1).isAir() &&
            !chunk.getBlock(x, y, z + 1).isAir();
    }


    private boolean isFaceVisible(Chunk chunk, int x, int y, int z, String face) {
        Block neighbor;
        switch (face) {
            case "up":
                neighbor = chunk.getBlock(x, y + 1, z);
                break;
            case "down":
                neighbor = chunk.getBlock(x, y - 1, z);
                break;
            case "north":
                neighbor = chunk.getBlock(x, y, z - 1);
                break;
            case "south":
                neighbor = chunk.getBlock(x, y, z + 1);
                break;
            case "west":
                neighbor = chunk.getBlock(x - 1, y, z);
                break;
            case "east":
                neighbor = chunk.getBlock(x + 1, y, z);
                break;
            default:
                return true; // Unknown face
        }
        return neighbor.isAir(); // Render the face if the neighboring block is air
    }

    public void dispose() {
        for (Pair<Chunk, Model> model : modelCache.values()) {
            model.getValue().dispose();
        }
        modelCache.clear();
    }

    public void cullChunks(Vector3 position) {
        modelCache.forEach((key, value) -> {
            Chunk chunk = value.getKey();
            if (Math.abs(chunk.getChunkX() - position.x) > 1 ||
                Math.abs(chunk.getChunkY() - position.y) > 1 ||
                Math.abs(chunk.getChunkZ() - position.z) > 1) {
                value.getValue().dispose();
            }
        });
        modelCache.keySet().removeIf(key -> {
            Chunk chunk = modelCache.get(key).getKey();
            return Math.abs(chunk.getChunkX() - position.x) > 1 ||
                Math.abs(chunk.getChunkY() - position.y) > 1 ||
                Math.abs(chunk.getChunkZ() - position.z) > 1;
        });
    }
}
