package inferno.cube_game.client.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;

public class DynamicSky {
    private Color skyColor;
    private float timeOfDay; // 0 to 1, where 0 is midnight and 1 is the next midnight

    public DynamicSky() {
        skyColor = new Color(0.1f, 0.1f, 0.2f, 1); // Dark color at night
        timeOfDay = 0; // Start at midnight
    }

    public void update(float deltaTime) {
        // Update the time of day
        timeOfDay += deltaTime / (60); // Simulate 1 in-game day every 60 seconds
        if (timeOfDay > 1) {
            timeOfDay = 0;
        }

        // Change sky color based on time of day
        updateSkyColor();
    }

    private void updateSkyColor() {
        Color morningColor = Color.valueOf("#87CEEB"); // Light blue
        Color middayColor = Color.valueOf("#00BFFF"); // Sky blue
        Color eveningColor = Color.valueOf("#FF4500"); // Orange
        Color nightColor = Color.valueOf("#000000"); // Dark color

        if (timeOfDay < 0.25f) { // Morning
            skyColor.set(morningColor.lerp(middayColor, timeOfDay / 0.25f));
        } else if (timeOfDay < 0.5f) { // Midday
            skyColor.set(middayColor.lerp(eveningColor, (timeOfDay - 0.25f) / 0.25f));
        } else if (timeOfDay < 0.75f) { // Evening
            skyColor.set(eveningColor.lerp(nightColor, (timeOfDay - 0.5f) / 0.25f));
        } else { // Night
            skyColor.set(nightColor.lerp(morningColor, (timeOfDay - 0.75f) / 0.25f));
        }
    }


    public void render() {
        Gdx.gl.glClearColor(skyColor.r, skyColor.g, skyColor.b, 1); // Clear with the sky color
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }
}

