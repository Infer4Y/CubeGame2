package inferno.cube_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public class InputHandler extends InputAdapter {
    private Camera camera;

    public InputHandler(Camera camera) {
        this.camera = camera;
    }

    public void update(float deltaTime) {
        // Mouse and keyboard handling as before...
        float deltaX = -Gdx.input.getDeltaX() * 0.2f;
        float deltaY = -Gdx.input.getDeltaY() * 0.2f;

        camera.direction.rotate(camera.up, deltaX);
        Vector3 right = camera.direction.cpy().crs(camera.up).nor();
        camera.direction.rotate(right, deltaY);

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.position.add(camera.direction.x * 5 * deltaTime, camera.direction.y * 5 * deltaTime, camera.direction.z * 5 * deltaTime);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.position.add(-camera.direction.x * 5 * deltaTime, -camera.direction.y * 5 * deltaTime, -camera.direction.z * 5 * deltaTime);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            // Move left: opposite of the right vector
            Vector3 left = right.cpy().scl(-5 * deltaTime); // Negative right vector (left direction)
            camera.position.add(left);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            // Move right: along the right vector
            Vector3 rightMove = right.scl(5 * deltaTime); // Positive right vector (right direction)
            camera.position.add(rightMove);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.input.setCursorCatched(false); // Unlock and show the cursor
        }
        // More input handling...

        camera.update();
    }
}
