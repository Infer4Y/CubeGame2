package inferno.cube_game.client.models;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import inferno.cube_game.Main;
import inferno.cube_game.extras.utils.Pair;
import inferno.cube_game.client.models.blocks.BlockModel;
import inferno.cube_game.client.models.blocks.BlockModel.Element;
import inferno.cube_game.client.models.blocks.FaceData;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.levels.chunks.Chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class GreedyMesher {
    private final Map<String, Material> materialCache = new HashMap<>();
    private final Map<String, Model> modelCache = new HashMap<>();
    private final Map<String, MeshPartBuilder> faceMeshPartBuilderCache = new HashMap<>();


    private String getChunkKey(Chunk chunk) {
        return chunk.getChunkX() + "," + chunk.getChunkY() + "," + chunk.getChunkZ();
    }

    public Model generateMesh(Chunk chunk, ModelBuilder modelBuilder) {
        String chunkKey = getChunkKey(chunk);

        // Check if the model is already cached
        if (modelCache.containsKey(chunkKey)) {
            return modelCache.get(chunkKey); // Return cached model
        }

        modelBuilder.begin();

        int chunkSize = Chunk.CHUNK_SIZE;

        IntStream.range(0, chunkSize).forEach(blockPositionX -> {
            IntStream.range(0, chunkSize).forEach(blockPositionY -> {
                IntStream.range(0, chunkSize).forEach(blockPositionZ -> {


                    Block block = chunk.getBlock(blockPositionX, blockPositionY, blockPositionZ);

                    // Block-level culling: skip if the block is completely surrounded by solid blocks
                    if (ClientChunkHelper.canCullBlock(chunk, blockPositionX, blockPositionY, blockPositionZ)) return; // Skip this block
                    if (block.isAir()) return; // Skip air blocks
                    if (!block.isSolid()) return; // Skip non-solid blocks

                    BlockModel blockModel = Main.blockModelOven.createOrGetBlockModel(block);

                    if (blockModel == null) return;
                    if (blockModel.textures.isEmpty()) return;
                    if (blockModel.elements.isEmpty()) return;

                    for (Element element : blockModel.elements) {
                        final var faceDirectionAndTextures = element.faces.entrySet();
                        for (var faceDirectionAndTextureEntry : faceDirectionAndTextures) {
                            String faceDirection = faceDirectionAndTextureEntry.getKey();
                            String textureKey = blockModel.textures.get(faceDirectionAndTextureEntry.getValue());

                            Pair<String, String> faceDirectionAndTexture = new Pair<>(faceDirection, textureKey);

                            makeMeshFace(chunk, modelBuilder, element, faceDirectionAndTexture,
                                blockPositionX, blockPositionY, blockPositionZ, faceMeshPartBuilderCache, block);
                        }
                    }
                });
            });
        });

        if (!faceMeshPartBuilderCache.isEmpty()) {
            faceMeshPartBuilderCache.clear();
        }

        Model model = modelBuilder.end();
        modelCache.put(chunkKey, model); // Cache the generated model
        return model;
    }

    private void makeMeshFace(Chunk chunk, ModelBuilder modelBuilder, Element element, Pair<String, String> faceDirectionAndTexture, int blockPositionX, int blockPositionY, int blockPositionZ,  Map<String, MeshPartBuilder> faceMeshPartBuilderCache, Block block) {
        if (!isFaceVisible(chunk, blockPositionX, blockPositionY, blockPositionZ, faceDirectionAndTexture.getKey())) return;

        // Out of the entire block, just the element's dimensions
        float elementWidth = element.to.x - element.from.x;
        float elementHeight = element.to.y - element.from.y;
        float elementDepth = element.to.z - element.from.z;

        // Get the position based on the element's dimensions and chunk position
        float chunkPositionX = element.from.x + blockPositionX;
        float chunkPositionY = element.from.y + blockPositionY;
        float chunkPositionZ = element.from.z + blockPositionZ;

        FaceData faceData = new FaceData(elementWidth,
            elementHeight,
            elementDepth,
            chunkPositionX,
            chunkPositionY,
            chunkPositionZ
        );

        // Create the faceDirectionAndTexture based on its direction and dynamic dimensions
        try {
            makeFaceFromDirection(modelBuilder, faceDirectionAndTexture, faceData, faceMeshPartBuilderCache, block);
        } catch (Exception ignored) {
        }
    }

    private void makeFaceFromDirection(ModelBuilder modelBuilder, Pair<String, String> faceDirectionAndTexture,
                                       FaceData faceData, Map<String, MeshPartBuilder> faceMeshPartBuilderCache, Block block) throws Exception {


        // Material caching
        String meshPartName = block.getDomain() + "_" + block.getRegistryName() + "_face_of_block_" + faceDirectionAndTexture.getKey();

        // Get the material from the face texture
        Material material = getMaterialFromFaceOfBlockModel(faceDirectionAndTexture.getValue());

        //if (!Objects.equals(material.id, faceDirectionAndTexture.getValue())) return;

        switch (faceDirectionAndTexture.getKey()) {
            case "up" -> {
                getMeshPartBuilder(modelBuilder, faceMeshPartBuilderCache, material, meshPartName).rect(
                    faceData.facePositionX - faceData.faceWidth / 2, faceData.facePositionY + faceData.faceHeight / 2, faceData.facePositionZ + faceData.faceDepth / 2,  // Top-left
                    faceData.facePositionX + faceData.faceWidth / 2, faceData.facePositionY + faceData.faceHeight / 2, faceData.facePositionZ + faceData.faceDepth / 2,  // Top-right
                    faceData.facePositionX + faceData.faceWidth / 2, faceData.facePositionY + faceData.faceHeight / 2, faceData.facePositionZ - faceData.faceDepth / 2,  // Bottom-right
                    faceData.facePositionX - faceData.faceWidth / 2, faceData.facePositionY + faceData.faceHeight / 2, faceData.facePositionZ - faceData.faceDepth / 2,  // Bottom-left
                    0, 1, 0 // Normal (up)
                );
                return;
            }
            case "down" -> {
                getMeshPartBuilder(modelBuilder, faceMeshPartBuilderCache, material, meshPartName).rect(
                    faceData.facePositionX + faceData.faceWidth / 2, faceData.facePositionY - faceData.faceHeight / 2, faceData.facePositionZ - faceData.faceDepth / 2,  // Top-left
                    faceData.facePositionX + faceData.faceWidth / 2, faceData.facePositionY - faceData.faceHeight / 2, faceData.facePositionZ + faceData.faceDepth / 2,  // Top-right
                    faceData.facePositionX - faceData.faceWidth / 2, faceData.facePositionY - faceData.faceHeight / 2, faceData.facePositionZ + faceData.faceDepth / 2,  // Bottom-right
                    faceData.facePositionX - faceData.faceWidth / 2, faceData.facePositionY - faceData.faceHeight / 2, faceData.facePositionZ - faceData.faceDepth / 2,  // Bottom-left
                    0, -1, 0 // Normal (down)
                );
                return;
            }
            // North face
            case "north" -> {
                getMeshPartBuilder(modelBuilder, faceMeshPartBuilderCache, material, meshPartName).rect(
                    faceData.facePositionX + faceData.faceWidth / 2, faceData.facePositionY - faceData.faceHeight / 2, faceData.facePositionZ - faceData.faceDepth / 2,  // Bottom-right
                    faceData.facePositionX - faceData.faceWidth / 2, faceData.facePositionY - faceData.faceHeight / 2, faceData.facePositionZ - faceData.faceDepth / 2,  // Bottom-left
                    faceData.facePositionX - faceData.faceWidth / 2, faceData.facePositionY + faceData.faceHeight / 2, faceData.facePositionZ - faceData.faceDepth / 2,  // Top-left
                    faceData.facePositionX + faceData.faceWidth / 2, faceData.facePositionY + faceData.faceHeight / 2, faceData.facePositionZ - faceData.faceDepth / 2,  // Top-right
                    0, 0, -1 // Normal (north)
                );
                return;
            }
            // South face
            case "south" -> {
                getMeshPartBuilder(modelBuilder, faceMeshPartBuilderCache, material, meshPartName).rect(
                    faceData.facePositionX - faceData.faceWidth / 2, faceData.facePositionY - faceData.faceHeight / 2, faceData.facePositionZ + faceData.faceDepth / 2,  // Bottom-left
                    faceData.facePositionX + faceData.faceWidth / 2, faceData.facePositionY - faceData.faceHeight / 2, faceData.facePositionZ + faceData.faceDepth / 2,  // Bottom-right
                    faceData.facePositionX + faceData.faceWidth / 2, faceData.facePositionY + faceData.faceHeight / 2, faceData.facePositionZ + faceData.faceDepth / 2,  // Top-right
                    faceData.facePositionX - faceData.faceWidth / 2, faceData.facePositionY + faceData.faceHeight / 2, faceData.facePositionZ + faceData.faceDepth / 2,  // Top-left
                    0, 0, 1 // Normal (south)
                );
                return;
            }
            case "west" -> {
                getMeshPartBuilder(modelBuilder, faceMeshPartBuilderCache, material, meshPartName).rect(
                    faceData.facePositionX - faceData.faceWidth / 2, faceData.facePositionY - faceData.faceHeight / 2, faceData.facePositionZ - faceData.faceDepth / 2,  // Bottom-right
                    faceData.facePositionX - faceData.faceWidth / 2, faceData.facePositionY - faceData.faceHeight / 2, faceData.facePositionZ + faceData.faceDepth / 2,  // Bottom-left
                    faceData.facePositionX - faceData.faceWidth / 2, faceData.facePositionY + faceData.faceHeight / 2, faceData.facePositionZ + faceData.faceDepth / 2,  // Top-left
                    faceData.facePositionX - faceData.faceWidth / 2, faceData.facePositionY + faceData.faceHeight / 2, faceData.facePositionZ - faceData.faceDepth / 2,  // Top-right
                    -1, 0, 0 // Normal (west)
                );
                return;
            }
            case "east" -> {
                getMeshPartBuilder(modelBuilder, faceMeshPartBuilderCache, material, meshPartName).rect(
                    faceData.facePositionX + faceData.faceWidth / 2, faceData.facePositionY - faceData.faceHeight / 2, faceData.facePositionZ + faceData.faceDepth / 2,  // bottom-left
                    faceData.facePositionX + faceData.faceWidth / 2, faceData.facePositionY - faceData.faceHeight / 2, faceData.facePositionZ - faceData.faceDepth / 2,  // bottom-right
                    faceData.facePositionX + faceData.faceWidth / 2, faceData.facePositionY + faceData.faceHeight / 2, faceData.facePositionZ - faceData.faceDepth / 2,  // top-right
                    faceData.facePositionX + faceData.faceWidth / 2, faceData.facePositionY + faceData.faceHeight / 2, faceData.facePositionZ + faceData.faceDepth / 2,  // top-left
                    1, 0, 0 // Normal (east)
                );
                return;
            }
        }
        throw new Exception("Unknown face: " + faceDirectionAndTexture.getKey());
    }

    /**
     * Get the material from the face texture
     *
     * @param faceTexturePath Path to the face texture
     * @return Material
     */
    public Material getMaterialFromFaceOfBlockModel(String faceTexturePath) {
        return materialCache.computeIfAbsent(faceTexturePath, key ->
            new Material(key, TextureAttribute.createDiffuse(Main.textureLoader.loadTexture(key)))
        );
    }

    /**
     * Get the mesh part builder for the face
     *
     * @param modelBuilder            ModelBuilder
     * @param faceMeshPartBuilderCache Cache for mesh part builders
     * @param materialForFace         Material for the face
     * @param meshPartName            Name of the mesh part
     * @return MeshPartBuilder
     */
    private MeshPartBuilder getMeshPartBuilder(ModelBuilder modelBuilder, Map<String, MeshPartBuilder> faceMeshPartBuilderCache, Material materialForFace, String meshPartName) {
        return faceMeshPartBuilderCache.computeIfAbsent(meshPartName, key -> modelBuilder.part(meshPartName, GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position
                | VertexAttributes.Usage.Normal
                | VertexAttributes.Usage.TextureCoordinates,
            materialForFace));
    }


    /**
     * Check if the face of the block is visible
     *
     * @param chunk Chunk
     * @param x     X coordinate of the block
     * @param y     Y coordinate of the block
     * @param z     Z coordinate of the block
     * @param face  Face direction
     * @return True if the face is visible, false otherwise
     */
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
        modelCache.values().forEach(Model::dispose);
        modelCache.clear();
    }

    public void cullChunks(Vector3 position) {
        modelCache.values().forEach(Model::dispose);
        modelCache.keySet().clear();
    }


    public void clearMaterialCache() {
        materialCache.clear();
    }
}
