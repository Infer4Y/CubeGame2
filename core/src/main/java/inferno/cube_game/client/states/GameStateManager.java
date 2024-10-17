package inferno.cube_game.client.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import inferno.cube_game.Main;

public class GameStateManager {
    private static GameState currentState;
    public static SpriteBatch batch;
    public static boolean running = true;

    public static void setState(GameState newState) {
        if (currentState != null) {
            currentState.dispose(); // Dispose current state
        }
        currentState = newState;
        currentState.create();
    }

    public static void update(float deltaTime) {
        if (currentState == null) return;
        currentState.update(deltaTime);

    }

    public static void render() {
        if (Main.fpsCounter == null) return;
        if (Main.font == null) return;
        try {
            if (currentState != null)
                currentState.render();
        } catch (Exception e) {
            Gdx.app.log("GameStateManager",
                currentState.toString().concat(" had an issue during rendering at : ").concat(e.getMessage()));
        }

        Main.fpsCounter.setText(Main.font, "FPS : " + Gdx.graphics.getFramesPerSecond() +
            "\nDelta Time : " + Gdx.graphics.getDeltaTime() +
            "\nMemory Usage : " + String.format("%.3f", Gdx.app.getJavaHeap() / 1024 / 1024 / 1024f) + "GB" +
            "\nPosition : " + currentState.camera.position.toString());

        if (currentState == null) return;
        if (currentState.camera == null) return;

        batch.begin();
        Main.font.draw(batch, Main.fpsCounter, 20f , Main.fpsCounter.height + 20f);
        batch.end();
    }

    public static void resize(int width, int height) {
        if ((currentState == null)) return;
        currentState.resize(width, height);
    }

    public static void dispose() {
        if (!(currentState == null)) {
            currentState.dispose();
        }
        running = false;
    }
}
