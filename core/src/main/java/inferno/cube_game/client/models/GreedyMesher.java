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
import java.util.stream.IntStream;

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
        Vector3 chunkPosition = new Vector3(chunk.getChunkX() * chunkSize, chunk.getChunkY() * chunkSize, chunk.getChunkZ() * chunkSize);
        IntStream.range(0, chunkSize).forEach(x ->
            IntStream.range(0, chunkSize).forEach(y ->
                IntStream.range(0, chunkSize).forEach(z -> {
            Block block = chunk.getBlock(x, y, z);

            // Block-level culling: skip if the block is completely surrounded by solid blocks
            if (canCullBlock(chunk, x, y, z)) return; // Skip this block
            if (block.isAir()) return; // Skip air blocks
            if (!block.isSolid()) return; // Skip non-solid blocks

            BlockModel blockModel = Main.blockModelOven.getBlockModel(block);

            if (blockModel == null) return;
            if (blockModel.textures.isEmpty()) return;
            if (blockModel.elements.isEmpty()) return;

            elementsToFaces(chunk, modelBuilder, blockModel, x, y, z, block, faceBatches);
        })));

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

    private void elementsToFaces(Chunk chunk, ModelBuilder modelBuilder, BlockModel blockModel, int x, int y, int z, Block block, Map<Material, MeshPartBuilder> faceBatches) {
        blockModel.elements.forEach(element ->
            element.faces.entrySet().forEach(faceEntry ->
                makeFace(chunk, modelBuilder, element, faceEntry, x, y, z, blockModel, block, faceBatches)
            )
        );
    }

    private void makeFace(Chunk chunk, ModelBuilder modelBuilder, Element element, Map.Entry<String, String> faceEntry, int x, int y, int z, BlockModel blockModel, Block block, Map<Material, MeshPartBuilder> faceBatches) {
        if (!isFaceVisible(chunk, x, y, z, faceEntry.getKey())) return;

        // Get the position based on the element's dimensions
        float width = element.to.x - element.from.x;
        float height = element.to.y - element.from.y;
        float depth = element.to.z - element.from.z;

        // Get the position based on the element's dimensions and chunk position
        float posX =  element.from.x + x;
        float posY =  element.from.y + y;
        float posZ =  element.from.z + z;

        // Create the face based on its direction and dynamic dimensions
        makeFaceFromDirection(modelBuilder, faceEntry, faceBatches, blockModel, block, x, y, z, posX, width, posY, height, posZ, depth);
    }

    private void makeFaceFromDirection(ModelBuilder modelBuilder, Map.Entry<String, String> faceEntry, Map<Material, MeshPartBuilder> faceBatches, BlockModel blockModel, Block block, int finalX, int finalY, int finalZ, float posX, float width, float posY, float height, float posZ, float depth) {
        String texturePath = blockModel.textures.get(faceEntry.getValue());

        // Material caching
        Material material = materialCache.computeIfAbsent(block.getRegistryName() + faceEntry.getKey() + texturePath, key ->
            new Pair<>(block,new Material(TextureAttribute.createDiffuse(Main.textureLoader.loadTexture(texturePath))))
        ).getValue();

        switch (faceEntry.getKey()) {
            case "up" -> {
                getMeshPartBuilder(modelBuilder, faceEntry, faceBatches, material, finalX, finalY, finalZ).rect(
                    posX - width / 2, posY + height / 2, posZ + depth / 2,  // Top-left
                    posX + width / 2, posY + height / 2, posZ + depth / 2,  // Top-right
                    posX + width / 2, posY + height / 2, posZ - depth / 2,  // Bottom-right
                    posX - width / 2, posY + height / 2, posZ - depth / 2,  // Bottom-left
                    0, 1, 0 // Normal (up)
                );
                return;
            }
            case "down" -> {
                getMeshPartBuilder(modelBuilder, faceEntry, faceBatches, material, finalX, finalY, finalZ).rect(
                    posX + width / 2, posY - height / 2, posZ - depth / 2,  // Top-left
                    posX + width / 2, posY - height / 2, posZ + depth / 2,  // Top-right
                    posX - width / 2, posY - height / 2, posZ + depth / 2,  // Bottom-right
                    posX - width / 2, posY - height / 2, posZ - depth / 2,  // Bottom-left
                    0, -1, 0 // Normal (down)
                );
                return;
            }
            // North face
            case "north" -> {
                getMeshPartBuilder(modelBuilder, faceEntry, faceBatches, material, finalX, finalY, finalZ).rect(
                    posX + width / 2, posY - height / 2, posZ - depth / 2,  // Bottom-right
                    posX - width / 2, posY - height / 2, posZ - depth / 2,  // Bottom-left
                    posX - width / 2, posY + height / 2, posZ - depth / 2,  // Top-left
                    posX + width / 2, posY + height / 2, posZ - depth / 2,  // Top-right
                    0, 0, -1 // Normal (north)
                );
                return;
            }
            // South face
            case "south" -> {
                getMeshPartBuilder(modelBuilder, faceEntry, faceBatches, material, finalX, finalY, finalZ).rect(
                    posX - width / 2, posY - height / 2, posZ + depth / 2,  // Bottom-left
                    posX + width / 2, posY - height / 2, posZ + depth / 2,  // Bottom-right
                    posX + width / 2, posY + height / 2, posZ + depth / 2,  // Top-right
                    posX - width / 2, posY + height / 2, posZ + depth / 2,  // Top-left
                    0, 0, 1 // Normal (south)
                );
                return;
            }
            case "west" -> {
                getMeshPartBuilder(modelBuilder, faceEntry, faceBatches, material, finalX, finalY, finalZ).rect(
                    posX - width / 2, posY - height / 2, posZ - depth / 2,  // Bottom-right
                    posX - width / 2, posY - height / 2, posZ + depth / 2,  // Bottom-left
                    posX - width / 2, posY + height / 2, posZ + depth / 2,  // Top-left
                    posX - width / 2, posY + height / 2, posZ - depth / 2,  // Top-right
                    -1, 0, 0 // Normal (west)
                );
                return;
            }
            case "east" -> {
                getMeshPartBuilder(modelBuilder, faceEntry, faceBatches, material, finalX, finalY, finalZ).rect(
                    posX + width / 2, posY - height / 2, posZ + depth / 2,  // bottom-left
                    posX + width / 2, posY - height / 2, posZ - depth / 2,  // bottom-right
                    posX + width / 2, posY + height / 2, posZ - depth / 2,  // top-right
                    posX + width / 2, posY + height / 2, posZ + depth / 2,  // top-left
                    1, 0, 0 // Normal (east)
                );
                return;
            }
        }
        System.out.println("Unknown face: " + faceEntry.getKey());
    }

    private static MeshPartBuilder getMeshPartBuilder(ModelBuilder modelBuilder, Map.Entry<String, String> faceEntry, Map<Material, MeshPartBuilder> faceBatches, Material material, int finalX, int finalY, int finalZ) {
        MeshPartBuilder meshBuilder = faceBatches.computeIfAbsent(material, mat ->
            modelBuilder.part("batch_" + "x_"+ finalX + "y_" + finalY + "z_" + finalZ + "face_" + faceEntry.getKey().toUpperCase(), GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                mat));
        return meshBuilder;
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
