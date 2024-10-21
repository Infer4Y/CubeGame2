package inferno.cube_game.client.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import inferno.cube_game.Main;
import inferno.cube_game.client.models.blocks.BlockModel;
import inferno.cube_game.client.models.blocks.BlockModel.Element;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.levels.chunks.Chunk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class GreedyMesher {
    private final ConcurrentHashMap<String, Material> materialCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Model> modelCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MeshPartBuilder> faceMeshPartBuilderCache = new ConcurrentHashMap<>();


    public Model generateMesh(Chunk chunk, ModelBuilder modelBuilder) {
        String chunkKey = new Vector3(chunk.getChunkX(), chunk.getChunkY(), chunk.getChunkZ()).toString();

        // Check if the model is already cached
        if (modelCache.get(chunkKey) != null) {
            return modelCache.get(chunkKey); // Return cached model
        }

        int chunkSize = Chunk.CHUNK_SIZE;

        modelBuilder.begin();

        IntStream.range(0, chunkSize * chunkSize * chunkSize).forEach(index -> {
            int blockPositionZ = index % chunkSize;
            int blockPositionY = (index / chunkSize) % chunkSize;
            int blockPositionX = index / (chunkSize * chunkSize);

            // Block-level culling: skip if the block is completely surrounded by solid blocks
            if (ClientChunkHelper.canCullBlock(chunk, blockPositionX, blockPositionY, blockPositionZ)) return; // Skip this block

            Block block = chunk.getBlock(blockPositionX, blockPositionY, blockPositionZ);

            if (block.isAir()) return; // Skip air or non-solid blocks

            BlockModel blockModel = Main.blockModelOven.createOrGetBlockModel(block);

            if (blockModel == null) return;
            if (blockModel.textures.isEmpty()) return;
            if (blockModel.elements.isEmpty()) return;

            blockModel.elements.forEach(element -> {
                element.faces.forEach((faceDirection, textureKey) -> {
                    if (isFaceNotVisible(chunk, blockPositionX, blockPositionY, blockPositionZ, faceDirection)) return;

                    makeMeshFace(modelBuilder, element, faceDirection, blockModel.textures.get(textureKey),
                        blockPositionX, blockPositionY, blockPositionZ, faceMeshPartBuilderCache, block);
                });
            });
        });


        Model model = modelBuilder.end();

        if (!faceMeshPartBuilderCache.isEmpty()) {
            faceMeshPartBuilderCache.clear();
        }
        modelCache.put(chunkKey, model); // Cache the generated model
        return model;
    }

    private void makeMeshFace(ModelBuilder modelBuilder, Element element, String face, String texture,
                              int blockPositionX, int blockPositionY, int blockPositionZ,
                              Map<String, MeshPartBuilder> faceMeshPartBuilderCache, Block block) {

        // Out of the entire block, just the element's dimensions
        float faceWidth = (element.to.x - element.from.x)  /2;
        float faceHeight = (element.to.y - element.from.y) /2;
        float faceDepth = (element.to.z - element.from.z)  /2;

        // Get the position based on the element's dimensions and chunk position
        float facePositionX = element.from.x + blockPositionX;
        float facePositionY = element.from.y + blockPositionY;
        float facePositionZ = element.from.z + blockPositionZ;

        // Create the meshPartName for this face
        String meshPartName = block.getDomain().concat("_").concat(block.getRegistryName()).concat("_").concat(face);

        // Get the material from the face texture
        MeshPartBuilder builder = faceMeshPartBuilderCache.computeIfAbsent(meshPartName, key -> {
            Material material = materialCache.computeIfAbsent(texture, tKey ->
                new Material(texture,
                    TextureAttribute.createDiffuse(Main.textureLoader.loadTexture(tKey)),
                    new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA))
            );

            //Gdx.app.log("greedymesher", meshPartName.concat(" ").concat(face).concat(" ").concat(texture));

            return modelBuilder.part(meshPartName, GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position |
                    VertexAttributes.Usage.Normal |
                    VertexAttributes.Usage.TextureCoordinates,
                material);
        });

        //if (!Objects.equals(material.id, texture)) return;

        // Refactor using a switch statement to handle the different faces
        switch (face) {
            case "top" ->
                makeTopFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "bottom" ->
                makeBottomFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "north" ->
                makeNorthFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "south" ->
                makeSouthFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "west" ->
                makeWestFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "east" ->
                makeEastFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
        }
    }


    private void makeEastFace(MeshPartBuilder meshPartBuilder, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        meshPartBuilder.rect(
            new Vector3(facePositionX + faceWidth, facePositionY - faceHeight, facePositionZ + faceDepth),  // bottom-left
            new Vector3(facePositionX + faceWidth, facePositionY - faceHeight, facePositionZ - faceDepth),  // bottom-right
            new Vector3(facePositionX + faceWidth, facePositionY + faceHeight, facePositionZ - faceDepth),  // top-right
            new Vector3(facePositionX + faceWidth, facePositionY + faceHeight, facePositionZ + faceDepth),  // top-left
            new Vector3(1, 0, 0) // Normal (east)
        );
    }

    private void makeWestFace(MeshPartBuilder meshPartBuilder, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        meshPartBuilder.rect(
            new Vector3(facePositionX - faceWidth, facePositionY - faceHeight, facePositionZ - faceDepth),  // Bottom-right
            new Vector3(facePositionX - faceWidth, facePositionY - faceHeight, facePositionZ + faceDepth),  // Bottom-left
            new Vector3(facePositionX - faceWidth, facePositionY + faceHeight, facePositionZ + faceDepth),  // Top-left
            new Vector3(facePositionX - faceWidth, facePositionY + faceHeight, facePositionZ - faceDepth),  // Top-right
            new Vector3(-1, 0, 0) // Normal (west)
        );
    }

    private void makeSouthFace(MeshPartBuilder meshPartBuilder, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        meshPartBuilder.rect(
            new Vector3(facePositionX - faceWidth, facePositionY - faceHeight, facePositionZ + faceDepth),  // Bottom-left
            new Vector3(facePositionX + faceWidth, facePositionY - faceHeight, facePositionZ + faceDepth),  // Bottom-right
            new Vector3(facePositionX + faceWidth, facePositionY + faceHeight, facePositionZ + faceDepth),  // Top-right
            new Vector3(facePositionX - faceWidth, facePositionY + faceHeight, facePositionZ + faceDepth),  // Top-left
            new Vector3(0, 0, 1)// Normal (south)
        );
    }

    private void makeNorthFace(MeshPartBuilder meshPartBuilder, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        meshPartBuilder.rect(
            new Vector3(facePositionX + faceWidth, facePositionY - faceHeight, facePositionZ - faceDepth),  // Bottom-right
            new Vector3(facePositionX - faceWidth, facePositionY - faceHeight, facePositionZ - faceDepth),  // Bottom-left
            new Vector3(facePositionX - faceWidth, facePositionY + faceHeight, facePositionZ - faceDepth),  // Top-left
            new Vector3(facePositionX + faceWidth, facePositionY + faceHeight, facePositionZ - faceDepth),  // Top-right
            new Vector3(0, 0, -1) // Normal (north)
        );
    }

    private void makeBottomFace(MeshPartBuilder meshPartBuilder, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        meshPartBuilder.rect(
            new Vector3(facePositionX + faceWidth, facePositionY - faceHeight, facePositionZ - faceDepth),  // Top-left
            new Vector3(facePositionX + faceWidth, facePositionY - faceHeight, facePositionZ + faceDepth),  // Top-right
            new Vector3(facePositionX - faceWidth, facePositionY - faceHeight, facePositionZ + faceDepth),  // Bottom-right
            new Vector3(facePositionX - faceWidth, facePositionY - faceHeight, facePositionZ - faceDepth),  // Bottom-left
            new Vector3(0, -1, 0) // Normal (down)
        );
    }

    private void makeTopFace(MeshPartBuilder meshPartBuilder, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        meshPartBuilder.rect(
            new Vector3(facePositionX - faceWidth, facePositionY + faceHeight, facePositionZ + faceDepth),  // Top-left
            new Vector3(facePositionX + faceWidth, facePositionY + faceHeight, facePositionZ + faceDepth),  // Top-right
            new Vector3(facePositionX + faceWidth, facePositionY + faceHeight, facePositionZ - faceDepth),  // Bottom-right
            new Vector3(facePositionX - faceWidth, facePositionY + faceHeight, facePositionZ - faceDepth),  // Bottom-left
            new Vector3(0, 1, 0) // Normal (up)
        );
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
    private boolean isFaceNotVisible(Chunk chunk, int x, int y, int z, String face) {
        Block neighbor;
        switch (face) {
            case "top":
                neighbor = chunk.getBlock(x, y + 1, z);
                break;
            case "bottom":
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
        return !neighbor.isAir() || !neighbor.isTransparent(); // Render the face if the neighboring block is air
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
