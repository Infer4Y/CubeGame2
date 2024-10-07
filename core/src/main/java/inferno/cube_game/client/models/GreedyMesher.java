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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GreedyMesher {
    private final Map<String, Pair<Block, Material>> materialCache = new ConcurrentHashMap<>();
    private final Map<String, Pair<Chunk, Model>> modelCache = new ConcurrentHashMap<>();


    private String getChunkKey(Chunk chunk) {
        return chunk.getChunkX() + "," + chunk.getChunkY() + "," + chunk.getChunkZ();
    }

    public synchronized Model generateMesh(Chunk chunk, ModelBuilder modelBuilder) {
        String chunkKey = getChunkKey(chunk);

        // Check if the model is already cached
        if (modelCache.containsKey(chunkKey)) {
            if (modelCache.get(chunkKey).getKey() != chunk) {
                modelCache.get(chunkKey).getValue().dispose(); // Dispose of the old model
            } else {
                return modelCache.get(chunkKey).getValue(); // Return cached model
            }
        }

        modelBuilder.begin();
        Map<Material, MeshPartBuilder> faceBatches = new HashMap<>();

        int chunkSize = Chunk.CHUNK_SIZE;
        Vector3 chunkPosition = new Vector3(chunk.getChunkX() * chunkSize, chunk.getChunkY() * chunkSize, chunk.getChunkZ() * chunkSize);

        for (int x = 0; x < chunkSize; x++) {
            for (int y = 0; y < chunkSize; y++) {
                for (int z = 0; z < chunkSize; z++) {
                    Block block = chunk.getBlock(x, y, z);

                    // Block-level culling: skip if the block is completely surrounded by solid blocks
                    if (canCullBlock(chunk, x, y, z)) {
                        continue; // Skip this block
                    }

                    if (block.isSolid() && !block.isAir()) {
                        BlockModel blockModel = Main.blockModelOven.getBlockModel(block);
                        if (blockModel != null && !blockModel.elements.isEmpty()) {
                            for (Element element : blockModel.elements) {
                                for (Map.Entry<String, String> faceEntry : element.faces.entrySet()) {
                                    String texturePath = blockModel.textures.get(faceEntry.getValue());

                                    // Material caching
                                    Material material = materialCache.computeIfAbsent(block.getRegistryName() + texturePath, key ->
                                        new Pair<>(block,new Material(TextureAttribute.createDiffuse(Main.textureLoader.loadTexture(texturePath))))
                                    ).getValue();

                                    int finalX = x;
                                    int finalY = y;
                                    int finalZ = z;
                                    MeshPartBuilder meshBuilder = faceBatches.computeIfAbsent(material, mat ->
                                        modelBuilder.part("batch-" + "x"+ finalX + "y" + finalY + "z" + finalZ + "face" + faceEntry.getKey() + mat.hashCode(), GL20.GL_TRIANGLES,
                                            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                                            mat));

                                    // Get the position based on the element's dimensions
                                    float width = element.to.x - element.from.x;
                                    float height = element.to.y - element.from.y;
                                    float depth = element.to.z - element.from.z;

                                    // Get the position based on the element's dimensions and chunk position
                                    float posX = chunkPosition.x + element.from.x + x;
                                    float posY = chunkPosition.y + element.from.y + y;
                                    float posZ = chunkPosition.z + element.from.z + z;

                                    // Create the face based on its direction and dynamic dimensions
                                    if (isFaceVisible(chunk, x, y, z, faceEntry.getKey())) {
                                        switch (faceEntry.getKey()) {
                                            case "up":
                                                meshBuilder.rect(
                                                    posX - width / 2, posY + height / 2, posZ + depth / 2,  // Top-left
                                                    posX + width / 2, posY + height / 2, posZ + depth / 2,  // Top-right
                                                    posX + width / 2, posY + height / 2, posZ - depth / 2,  // Bottom-right
                                                    posX - width / 2, posY + height / 2, posZ - depth / 2,  // Bottom-left
                                                    0, 1, 0 // Normal (up)
                                                );
                                                break;
                                            case "down":
                                                meshBuilder.rect(
                                                    posX + width / 2, posY - height / 2, posZ - depth / 2,  // Top-left
                                                    posX + width / 2, posY - height / 2, posZ + depth / 2,  // Top-right
                                                    posX - width / 2, posY - height / 2, posZ + depth / 2,  // Bottom-right
                                                    posX - width / 2, posY - height / 2, posZ - depth / 2,  // Bottom-left
                                                    0, -1, 0 // Normal (down)
                                                );
                                                break;
                                            // North face
                                            case "north":
                                                meshBuilder.rect(
                                                    posX + width / 2, posY - height / 2, posZ - depth / 2,  // Bottom-right
                                                    posX - width / 2, posY - height / 2, posZ - depth / 2,  // Bottom-left
                                                    posX - width / 2, posY + height / 2, posZ - depth / 2,  // Top-left
                                                    posX + width / 2, posY + height / 2, posZ - depth / 2,  // Top-right
                                                    0, 0, -1 // Normal (north)
                                                );
                                                break;
                                            // South face
                                            case "south":
                                                meshBuilder.rect(
                                                    posX - width / 2, posY - height / 2, posZ + depth / 2,  // Bottom-left
                                                    posX + width / 2, posY - height / 2, posZ + depth / 2,  // Bottom-right
                                                    posX + width / 2, posY + height / 2, posZ + depth / 2,  // Top-right
                                                    posX - width / 2, posY + height / 2, posZ + depth / 2,  // Top-left
                                                    0, 0, 1 // Normal (south)
                                                );
                                                break;
                                            case "west":
                                                meshBuilder.rect(
                                                    posX - width / 2, posY - height / 2, posZ - depth / 2,  // Bottom-right
                                                    posX - width / 2, posY - height / 2, posZ + depth / 2,  // Bottom-left
                                                    posX - width / 2, posY + height / 2, posZ + depth / 2,  // Top-left
                                                    posX - width / 2, posY + height / 2, posZ - depth / 2,  // Top-right
                                                    -1, 0, 0 // Normal (west)
                                                );
                                                break;
                                            case "east":
                                                meshBuilder.rect(
                                                    posX + width / 2, posY - height / 2, posZ + depth / 2,  // bottom-left
                                                    posX + width / 2, posY - height / 2, posZ - depth / 2,  // bottom-right
                                                    posX + width / 2, posY + height / 2, posZ - depth / 2,  // top-right
                                                    posX + width / 2, posY + height / 2, posZ + depth / 2,  // top-left
                                                    1, 0, 0 // Normal (east)
                                                );
                                                break;
                                            default:
                                                System.out.println("Unknown face: " + faceEntry.getKey());
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Model model = modelBuilder.end();
        modelCache.put(chunkKey, new Pair<>(chunk, model)); // Cache the generated model
        return model;
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
