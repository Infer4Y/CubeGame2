package inferno.cube_game.client.models.blocks;

/**
 * FaceData class that represents the data for a face of a block model.
 * @see BlockModel
 * @see BlockModel.Element
 * @author inferno4you
 */
public class FaceData {
    /**
     * Width of the face : 0-16 (0 is the left, 16 is the right) X axis
     * Height of the face : 0-16 (0 is the bottom, 16 is the top) Y axis
     * Depth of the face : 0-16 (0 is the front, 16 is the back) Z axis
     */
    public float faceWidth;
    public float faceHeight;
    public float faceDepth;

    /**
     * Position of the face : 0-16 (0 is the left, 16 is the right) X axis
     * Position of the face : 0-16 (0 is the bottom, 16 is the top) Y axis
     * Position of the face : 0-16 (0 is the front, 16 is the back) Z axis
     */
    public float facePositionX;
    public float facePositionY;
    public float facePositionZ;

    /**
     * Create a new FaceData object
     * @param faceWidth Width of the face
     * @param faceHeight Height of the face
     * @param faceDepth Depth of the face
     * @param facePositionX Position of the face on the X axis
     * @param facePositionY Position of the face on the Y axis
     * @param facePositionZ Position of the face on the Z axis
     */
    public FaceData(float faceWidth, float faceHeight, float faceDepth, float facePositionX, float facePositionY, float facePositionZ) {
        this.faceWidth = faceWidth;
        this.faceHeight = faceHeight;
        this.faceDepth = faceDepth;
        this.facePositionX = facePositionX;
        this.facePositionY = facePositionY;
        this.facePositionZ = facePositionZ;
    }
}
