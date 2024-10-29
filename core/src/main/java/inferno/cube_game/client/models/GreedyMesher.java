package inferno.cube_game.client.models;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
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

public class GreedyMesher {
    private final ConcurrentHashMap<String, Material> materialCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Model> modelCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MeshPartBuilder> meshCache = new ConcurrentHashMap<>();

    public Model generateMesh(Chunk chunk) {
        final ModelBuilder modelBuilder = new ModelBuilder();
        String chunkKey = new Vector3(chunk.getChunkX(), chunk.getChunkY(), chunk.getChunkZ()).toString();

        // Return cached model if it exists
        Model cachedModel = modelCache.get(chunkKey);
        if (cachedModel != null) {
            return cachedModel;
        }

        int chunkSize = Chunk.CHUNK_SIZE;

        modelBuilder.begin();

        boolean hasVisibleFaces = false;

        for (int blockPositionX = 0; blockPositionX < chunkSize; blockPositionX++) {
            for (int blockPositionY = 0; blockPositionY < chunkSize; blockPositionY++) {
                for (int blockPositionZ = 0; blockPositionZ < chunkSize; blockPositionZ++) {
                    Block block = chunk.getBlock(blockPositionX, blockPositionY, blockPositionZ);

                    if (block == null || block.isAir() || chunk.canCullBlock(blockPositionX, blockPositionY, blockPositionZ)) continue;

                    BlockModel blockModel = Main.blockModelOven.createOrGetBlockModel(block);

                    if (blockModel == null || blockModel.textures.isEmpty() || blockModel.elements.isEmpty()) continue;

                    modelBuilder.node().id = block.getDomain().concat(":").concat(block.getRegistryName());
                    for (Element element : blockModel.elements) {
                        for (Map.Entry<String, String> faceEntry : element.faces.entrySet()) {
                            String faceDirection = faceEntry.getKey();

                            if (chunk.isFaceNotVisible(blockPositionX, blockPositionY, blockPositionZ, faceDirection)) continue;

                            String textureKey = faceEntry.getValue();
                            makeMeshFace(modelBuilder, element, faceDirection, blockModel.textures.get(textureKey),
                                blockPositionX, blockPositionY, blockPositionZ, block);
                            hasVisibleFaces = true;
                        }
                    }
                }
            }
        }

        Model model = modelBuilder.end();

        clearMaterialCache();
        meshCache.clear();

        if (!hasVisibleFaces) return null;

        return modelCache.put(chunkKey, model);
    }

    private void makeMeshFace(ModelBuilder modelBuilder, Element element, String face, String texture,
                              int blockPositionX, int blockPositionY, int blockPositionZ, Block block) {
        float faceWidth = (element.to.x - element.from.x) / 2;
        float faceHeight = (element.to.y - element.from.y) / 2;
        float faceDepth = (element.to.z - element.from.z) / 2;

        float facePositionX = element.from.x + blockPositionX;
        float facePositionY = element.from.y + blockPositionY;
        float facePositionZ = element.from.z + blockPositionZ;

        String meshPartName = block.getDomain().concat("_").concat(block.getRegistryName()).concat("_").concat(face);
        Material material = materialCache.computeIfAbsent(texture, tKey -> new Material(
            TextureAttribute.createDiffuse(Main.textureLoader.loadTexture(tKey)),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        ));

        modelBuilder.node().id = meshPartName;
        MeshPartBuilder builder = modelBuilder.part(meshPartName,
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            material);
            //meshCache.computeIfAbsent( meshPartName, key -> modelBuilder.part(meshPartName, GL20.GL_TRIANGLES,
            //VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            //material)); // this is faster but has issues since it will use random to me and other materials

        switch (face) {
            case "top" -> makeTopFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "bottom" -> makeBottomFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "north" -> makeNorthFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "south" -> makeSouthFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "west" -> makeWestFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "east" -> makeEastFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
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

    public void cullChunks(Vector3 position) {
        modelCache.values().forEach(Model::dispose);
        modelCache.keySet().clear();
    }

    public void clearMaterialCache() {
        materialCache.clear();
    }

    public void dispose() {
        modelCache.values().forEach(Model::dispose);
        modelCache.clear();
        materialCache.clear();
    }
}
