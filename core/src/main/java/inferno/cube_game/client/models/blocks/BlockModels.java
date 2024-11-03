package inferno.cube_game.client.models.blocks;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import inferno.cube_game.Main;
import inferno.cube_game.common.blocks.Block;

import java.util.HashMap;
import java.util.Map;

public class BlockModels {
    private static final HashMap<Block, Model> blockModels = new HashMap<>();


    public static Model getBlockModel(final Block block) {
        return blockModels.get(block);
    }

    public static void addBlockModel(final Block block, final BlockModel blockModel) {
        ModelBuilder modelBuilder = new ModelBuilder();

        modelBuilder.begin();
        for (BlockModel.Element element : blockModel.elements) {
            for (Map.Entry<String, String> faceEntry : element.faces.entrySet()) {
                String faceDirection = faceEntry.getKey();

                String textureKey = faceEntry.getValue();
                makeMeshFace(modelBuilder, element, faceDirection, blockModel.textures.get(textureKey),
                    block);
            }
        }
        blockModels.put(block, modelBuilder.end());
    }

    private static void makeMeshFace(ModelBuilder modelBuilder, BlockModel.Element element, String face, String texture, Block block) {
        float faceWidth = (element.to.x - element.from.x) / 2;
        float faceHeight = (element.to.y - element.from.y) / 2;
        float faceDepth = (element.to.z - element.from.z) / 2;

        float facePositionX = element.from.x;
        float facePositionY = element.from.y;
        float facePositionZ = element.from.z;

        Material material = new Material(
            TextureAttribute.createDiffuse(Main.textureLoader.loadTexture(texture)),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        );

        MeshPartBuilder builder = modelBuilder.part(face,
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            material);

        switch (face) {
            case "top" -> makeTopFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "bottom" -> makeBottomFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "north" -> makeNorthFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "south" -> makeSouthFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "west" -> makeWestFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
            case "east" -> makeEastFace(builder, facePositionX, faceWidth, facePositionY, faceHeight, facePositionZ, faceDepth);
        }
    }

    private static void makeEastFace(MeshPartBuilder meshPartBuilder, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        meshPartBuilder.rect(
            new Vector3(facePositionX + faceWidth, facePositionY - faceHeight, facePositionZ + faceDepth),  // bottom-left
            new Vector3(facePositionX + faceWidth, facePositionY - faceHeight, facePositionZ - faceDepth),  // bottom-right
            new Vector3(facePositionX + faceWidth, facePositionY + faceHeight, facePositionZ - faceDepth),  // top-right
            new Vector3(facePositionX + faceWidth, facePositionY + faceHeight, facePositionZ + faceDepth),  // top-left
            new Vector3(1, 0, 0) // Normal (east)
        );
    }

    private static void makeWestFace(MeshPartBuilder meshPartBuilder, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        meshPartBuilder.rect(
            new Vector3(facePositionX - faceWidth, facePositionY - faceHeight, facePositionZ - faceDepth),  // Bottom-right
            new Vector3(facePositionX - faceWidth, facePositionY - faceHeight, facePositionZ + faceDepth),  // Bottom-left
            new Vector3(facePositionX - faceWidth, facePositionY + faceHeight, facePositionZ + faceDepth),  // Top-left
            new Vector3(facePositionX - faceWidth, facePositionY + faceHeight, facePositionZ - faceDepth),  // Top-right
            new Vector3(-1, 0, 0) // Normal (west)
        );
    }

    private static void makeSouthFace(MeshPartBuilder meshPartBuilder, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        meshPartBuilder.rect(
            new Vector3(facePositionX - faceWidth, facePositionY - faceHeight, facePositionZ + faceDepth),  // Bottom-left
            new Vector3(facePositionX + faceWidth, facePositionY - faceHeight, facePositionZ + faceDepth),  // Bottom-right
            new Vector3(facePositionX + faceWidth, facePositionY + faceHeight, facePositionZ + faceDepth),  // Top-right
            new Vector3(facePositionX - faceWidth, facePositionY + faceHeight, facePositionZ + faceDepth),  // Top-left
            new Vector3(0, 0, 1)// Normal (south)
        );
    }

    private static void makeNorthFace(MeshPartBuilder meshPartBuilder, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        meshPartBuilder.rect(
            new Vector3(facePositionX + faceWidth, facePositionY - faceHeight, facePositionZ - faceDepth),  // Bottom-right
            new Vector3(facePositionX - faceWidth, facePositionY - faceHeight, facePositionZ - faceDepth),  // Bottom-left
            new Vector3(facePositionX - faceWidth, facePositionY + faceHeight, facePositionZ - faceDepth),  // Top-left
            new Vector3(facePositionX + faceWidth, facePositionY + faceHeight, facePositionZ - faceDepth),  // Top-right
            new Vector3(0, 0, -1) // Normal (north)
        );
    }

    private static void makeBottomFace(MeshPartBuilder meshPartBuilder, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        meshPartBuilder.rect(
            new Vector3(facePositionX + faceWidth, facePositionY - faceHeight, facePositionZ - faceDepth),  // Top-left
            new Vector3(facePositionX + faceWidth, facePositionY - faceHeight, facePositionZ + faceDepth),  // Top-right
            new Vector3(facePositionX - faceWidth, facePositionY - faceHeight, facePositionZ + faceDepth),  // Bottom-right
            new Vector3(facePositionX - faceWidth, facePositionY - faceHeight, facePositionZ - faceDepth),  // Bottom-left
            new Vector3(0, -1, 0) // Normal (down)
        );
    }

    private static void makeTopFace(MeshPartBuilder meshPartBuilder, float facePositionX, float faceWidth, float facePositionY, float faceHeight, float facePositionZ, float faceDepth) {
        meshPartBuilder.rect(
            new Vector3(facePositionX - faceWidth, facePositionY + faceHeight, facePositionZ + faceDepth),  // Top-left
            new Vector3(facePositionX + faceWidth, facePositionY + faceHeight, facePositionZ + faceDepth),  // Top-right
            new Vector3(facePositionX + faceWidth, facePositionY + faceHeight, facePositionZ - faceDepth),  // Bottom-right
            new Vector3(facePositionX - faceWidth, facePositionY + faceHeight, facePositionZ - faceDepth),  // Bottom-left
            new Vector3(0, 1, 0) // Normal (up)
        );
    }

    public static void dispose(){
        blockModels.values().forEach(Model::dispose);
    }
}
