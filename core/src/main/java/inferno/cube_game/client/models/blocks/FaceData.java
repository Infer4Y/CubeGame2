package inferno.cube_game.client.models.blocks;

public class FaceData {
    public float faceWidth;
    public float faceHeight;
    public float faceDepth;

    public float facePositionX;
    public float facePositionY;
    public float facePositionZ;

    public FaceData(float faceWidth, float faceHeight, float faceDepth, float facePositionX, float facePositionY, float facePositionZ) {
        this.faceWidth = faceWidth;
        this.faceHeight = faceHeight;
        this.faceDepth = faceDepth;
        this.facePositionX = facePositionX;
        this.facePositionY = facePositionY;
        this.facePositionZ = facePositionZ;
    }
}
