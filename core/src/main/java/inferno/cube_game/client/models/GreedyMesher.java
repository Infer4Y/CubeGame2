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
import inferno.cube_game.client.models.blocks.BlockModel;
import inferno.cube_game.client.models.blocks.BlockModel.Element;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.levels.chunks.Chunk;
import inferno.cube_game.common.registries.BlockRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GreedyMesher {
    private final ConcurrentHashMap<String, Material> materialCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Model> modelCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MeshPartBuilder> faceMeshPartBuilderCache = new ConcurrentHashMap<>();


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

        IntStream.range(0, chunkSize * chunkSize * chunkSize).forEach(index -> {
            int blockPositionZ = index % chunkSize;
            int blockPositionY = (index / chunkSize) % chunkSize;
            int blockPositionX = index / (chunkSize * chunkSize);

            // Block-level culling: skip if the block is completely surrounded by solid blocks
            if (ClientChunkHelper.canCullBlock(chunk, blockPositionX, blockPositionY, blockPositionZ)) return; // Skip this block

            Block block = chunk.getBlock(blockPositionX, blockPositionY, blockPositionZ);

            if (block.isAir() || !block.isSolid()) return; // Skip air or non-solid blocks

            BlockModel blockModel = Main.blockModelOven.createOrGetBlockModel(block);

            if (blockModel == null) return;
            if (blockModel.textures.isEmpty()) return;
            if (blockModel.elements.isEmpty()) return;

            blockModel.elements.forEach(element -> {
                element.faces.forEach((faceDirection, textureKey) -> {
                    makeMeshFace(chunk, modelBuilder, element, faceDirection, blockModel.textures.get(textureKey),
                        blockPositionX, blockPositionY, blockPositionZ, faceMeshPartBuilderCache, block);
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

    private void makeMeshFace(Chunk chunk, ModelBuilder modelBuilder, Element element, String face, String texture,
                              int blockPositionX, int blockPositionY, int blockPositionZ,
                              Map<String, MeshPartBuilder> faceMeshPartBuilderCache, Block block) {

        // Out of the entire block, just the element's dimensions
        float faceWidth = element.to.x - element.from.x;
        float faceHeight = element.to.y - element.from.y;
        float faceDepth = element.to.z - element.from.z;

        // Get the position based on the element's dimensions and chunk position
        float facePositionX = element.from.x + blockPositionX;
        float facePositionY = element.from.y + blockPositionY;
        float facePositionZ = element.from.z + blockPositionZ;

        // Create the meshPartName for this face
        String meshPartName = block.getDomain() + "_" + block.getRegistryName() + "_face_of_block_" + face;

        // Get the material from the face texture
        Material material = materialCache.computeIfAbsent(texture, key ->
            new Material(key, TextureAttribute.createNormal(Main.textureLoader.loadTexture(key)),
                TextureAttribute.createDiffuse(Main.textureLoader.loadTexture(key)))
        );

        if (!Objects.equals(material.id, texture)) return;

        // Refactor using a switch statement to handle the different faces
        if (!isFaceVisible(chunk, blockPositionX, blockPositionY, blockPositionZ, face)) return;
        switch (face) {
            case "up" -> {
                makeUpFace(modelBuilder, faceMeshPartBuilderCache, material, meshPartName, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            }
            case "down" -> {
                if (!isFaceVisible(chunk, blockPositionX, blockPositionY, blockPositionZ, "down")) break;
                makeDownFace(modelBuilder, faceMeshPartBuilderCache, material, meshPartName, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            }
            case "north" -> {
                if (!isFaceVisible(chunk, blockPositionX, blockPositionY, blockPositionZ, "north")) break;
                makeNorthFace(modelBuilder, faceMeshPartBuilderCache, material, meshPartName, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            }
            case "south" -> {
                if (!isFaceVisible(chunk, blockPositionX, blockPositionY, blockPositionZ, "south")) break;
                makeSouthFace(modelBuilder, faceMeshPartBuilderCache, material, meshPartName, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            }
            case "west" -> {
                if (!isFaceVisible(chunk, blockPositionX, blockPositionY, blockPositionZ, "west")) break;
                makeWestFace(modelBuilder, faceMeshPartBuilderCache, material, meshPartName, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            }
            case "east" -> {
                if (!isFaceVisible(chunk, blockPositionX, blockPositionY, blockPositionZ, "east")) break;
                makeEastFace(modelBuilder, faceMeshPartBuilderCache, material, meshPartName, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            }
        }
    }


    private void makeEastFace(ModelBuilder modelBuilder, Map<String, MeshPartBuilder> faceMeshPartBuilderCache, Material material, String meshPartName, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        getMeshPartBuilder(modelBuilder, faceMeshPartBuilderCache, material, meshPartName).rect(
            facePositionX + faceWidth / 2, facePositionY - faceHeight / 2, facePositionZ + faceDepth / 2,  // bottom-left
            facePositionX + faceWidth / 2, facePositionY - faceHeight / 2, facePositionZ - faceDepth / 2,  // bottom-right
            facePositionX + faceWidth / 2, facePositionY + faceHeight / 2, facePositionZ - faceDepth / 2,  // top-right
            facePositionX + faceWidth / 2, facePositionY + faceHeight / 2, facePositionZ + faceDepth / 2,  // top-left
            1, 0, 0 // Normal (east)
        );
    }

    private void makeWestFace(ModelBuilder modelBuilder, Map<String, MeshPartBuilder> faceMeshPartBuilderCache, Material material, String meshPartName, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        getMeshPartBuilder(modelBuilder, faceMeshPartBuilderCache, material, meshPartName).rect(
            facePositionX - faceWidth / 2, facePositionY - faceHeight / 2, facePositionZ - faceDepth / 2,  // Bottom-right
            facePositionX - faceWidth / 2, facePositionY - faceHeight / 2, facePositionZ + faceDepth / 2,  // Bottom-left
            facePositionX - faceWidth / 2, facePositionY + faceHeight / 2, facePositionZ + faceDepth / 2,  // Top-left
            facePositionX - faceWidth / 2, facePositionY + faceHeight / 2, facePositionZ - faceDepth / 2,  // Top-right
            -1, 0, 0 // Normal (west)
        );
    }

    private void makeSouthFace(ModelBuilder modelBuilder, Map<String, MeshPartBuilder> faceMeshPartBuilderCache, Material material, String meshPartName, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        getMeshPartBuilder(modelBuilder, faceMeshPartBuilderCache, material, meshPartName).rect(
            facePositionX - faceWidth / 2, facePositionY - faceHeight / 2, facePositionZ + faceDepth / 2,  // Bottom-left
            facePositionX + faceWidth / 2, facePositionY - faceHeight / 2, facePositionZ + faceDepth / 2,  // Bottom-right
            facePositionX + faceWidth / 2, facePositionY + faceHeight / 2, facePositionZ + faceDepth / 2,  // Top-right
            facePositionX - faceWidth / 2, facePositionY + faceHeight / 2, facePositionZ + faceDepth / 2,  // Top-left
            0, 0, 1 // Normal (south)
        );
    }

    private void makeNorthFace(ModelBuilder modelBuilder, Map<String, MeshPartBuilder> faceMeshPartBuilderCache, Material material, String meshPartName, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        getMeshPartBuilder(modelBuilder, faceMeshPartBuilderCache, material, meshPartName).rect(
            facePositionX + faceWidth / 2, facePositionY - faceHeight / 2, facePositionZ - faceDepth / 2,  // Bottom-right
            facePositionX - faceWidth / 2, facePositionY - faceHeight / 2, facePositionZ - faceDepth / 2,  // Bottom-left
            facePositionX - faceWidth / 2, facePositionY + faceHeight / 2, facePositionZ - faceDepth / 2,  // Top-left
            facePositionX + faceWidth / 2, facePositionY + faceHeight / 2, facePositionZ - faceDepth / 2,  // Top-right
            0, 0, -1 // Normal (north)
        );
    }

    private void makeDownFace(ModelBuilder modelBuilder, Map<String, MeshPartBuilder> faceMeshPartBuilderCache, Material material, String meshPartName, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        getMeshPartBuilder(modelBuilder, faceMeshPartBuilderCache, material, meshPartName).rect(
            facePositionX + faceWidth / 2, facePositionY - faceHeight / 2, facePositionZ - faceDepth / 2,  // Top-left
            facePositionX + faceWidth / 2, facePositionY - faceHeight / 2, facePositionZ + faceDepth / 2,  // Top-right
            facePositionX - faceWidth / 2, facePositionY - faceHeight / 2, facePositionZ + faceDepth / 2,  // Bottom-right
            facePositionX - faceWidth / 2, facePositionY - faceHeight / 2, facePositionZ - faceDepth / 2,  // Bottom-left
            0, -1, 0 // Normal (down)
        );
    }

    private void makeUpFace(ModelBuilder modelBuilder, Map<String, MeshPartBuilder> faceMeshPartBuilderCache, Material material, String meshPartName, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        getMeshPartBuilder(modelBuilder, faceMeshPartBuilderCache, material, meshPartName).rect(
            facePositionX - faceWidth / 2, facePositionY + faceHeight / 2, facePositionZ + faceDepth / 2,  // Top-left
            facePositionX + faceWidth / 2, facePositionY + faceHeight / 2, facePositionZ + faceDepth / 2,  // Top-right
            facePositionX + faceWidth / 2, facePositionY + faceHeight / 2, facePositionZ - faceDepth / 2,  // Bottom-right
            facePositionX - faceWidth / 2, facePositionY + faceHeight / 2, facePositionZ - faceDepth / 2,  // Bottom-left
            0, 1, 0 // Normal (up)
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
                return false; // Unknown face
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
