package inferno.cube_game.client.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

public class CursorAxisRender {
    private final ShapeRenderer shapeRenderer;
    private final float distanceFromCamera = 50f; // Distance in front of the camera
    private Camera camera;

    public CursorAxisRender(Camera camera) {
        shapeRenderer = new ShapeRenderer();
        this.camera = camera;
    }

    public void renderAxisLinesForPlayerCursor() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        Vector3 camPos = new Vector3(camera.position);
        Vector3 camDir = new Vector3(camera.direction).nor(); // Get camera direction and normalize it

        // Offset the axes slightly in front of the camera
        Vector3 centerPosition = camPos.add(camDir.scl(distanceFromCamera));

        // X Axis (Red)
        shapeRenderer.setColor(1, 0, 0, 1); // Red for X axis
        shapeRenderer.line(centerPosition.x, centerPosition.y, centerPosition.z,
            centerPosition.x + 2, centerPosition.y, centerPosition.z);

        // Y Axis (Green)
        shapeRenderer.setColor(0, 1, 0, 1); // Green for Y axis
        shapeRenderer.line(centerPosition.x, centerPosition.y, centerPosition.z,
            centerPosition.x, centerPosition.y + 2, centerPosition.z);

        // Z Axis (Blue)
        shapeRenderer.setColor(0, 0, 1, 1); // Blue for Z axis
        shapeRenderer.line(centerPosition.x, centerPosition.y, centerPosition.z,
            centerPosition.x, centerPosition.y, centerPosition.z + 2);

        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
