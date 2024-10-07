package inferno.cube_game.client.states;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import inferno.cube_game.InputHandler;

public abstract class GameState {
    protected SpriteBatch batch;
    protected Camera camera;
    protected Viewport viewport;

    public GameState(SpriteBatch batch) {
        this.batch = batch;

        // Set up the camera and viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(1920, 1080, camera); // Adjust the resolution here
        viewport.apply();

        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
    }

    public void resize(int width, int height) {
        // Update the viewport and camera when the window is resized
        viewport.update(width, height);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
    }

    // Abstract methods to be implemented by each game state
    public void create(){
        camera = new OrthographicCamera();
        viewport = new FitViewport(1920, 1080, camera); // Adjust the resolution here
        viewport.apply();

        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
    };

    public abstract void render();

    public abstract void update(float deltaTime);

    public abstract void dispose();
}

