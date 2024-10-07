package inferno.cube_game.client.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import inferno.cube_game.Main;

public class GameStateManager {
    private static GameState currentState;
    public static SpriteBatch batch;

    public static void setState(GameState newState) {
        if (currentState != null) {
            currentState.dispose(); // Dispose current state
        }
        currentState = newState;
        currentState.create();
    }

    public static void update(float deltaTime) {
        if (currentState != null) {
            currentState.update(deltaTime);
        }
    }

    public static void render() {
        if (currentState != null) {
            currentState.render();
        }

        Main.fpsCounter.setText(Main.font, "FPS : " + Gdx.graphics.getFramesPerSecond());

        batch.begin();
        Main.font.draw(batch, Main.fpsCounter, 20f , 20f);
        batch.end();
    }

    public static void resize(int width, int height) {
        if (currentState != null) {
            currentState.resize(width, height);
        }
    }

    public static void dispose() {
        if (currentState != null) {
            currentState.dispose();
        }
    }
}
