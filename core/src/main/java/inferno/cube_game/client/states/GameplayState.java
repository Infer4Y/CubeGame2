package inferno.cube_game.client.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import inferno.cube_game.InputHandler;
import inferno.cube_game.client.render.DynamicSky;
import inferno.cube_game.client.render.WorldRenderer;

public class GameplayState extends GameState {
    public static final DirectionalLight DIRECTIONAL_LIGHT = new DirectionalLight().set(1f, 1f, 1f, -1f, -1f, -1f);
    private WorldRenderer worldRenderer;
    private Environment environment;
    private ShapeRenderer shapeRenderer;
    private float distanceFromCamera = 50f; // Distance in front of the camera
    private DynamicSky dynamicSky;

    public GameplayState(SpriteBatch batch) {
        super(batch);
        this.shapeRenderer = new ShapeRenderer();
        this.dynamicSky = new DynamicSky();
    }

    @Override
    public void create() {
        environment = new Environment();
        environment.add(DIRECTIONAL_LIGHT);
        environment.set(
            new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f),
            new ColorAttribute(ColorAttribute.Fog, 0.5f, 0.5f, 0.5f, 1f),
            new ColorAttribute(ColorAttribute.Reflection, 0.8f, 0.8f, 0.8f, 1f),
            new ColorAttribute(ColorAttribute.Ambient, 0.8f, 0.8f, 0.8f, 1f)
        );

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        camera.position.set(0, 0f, 0); // Position the camera above the blocks
        //camera.lookAt(0f, 0f, 0f); // Look towards the blocks
        camera.near = 0.1f;
        camera.far = 1000f;
        camera.update();

        // Initialize the world renderer
        worldRenderer = new WorldRenderer(camera, environment);
    }

    @Override
    public void render() {
        dynamicSky.render();
        camera.viewportWidth = Gdx.graphics.getWidth();
        camera.viewportHeight = Gdx.graphics.getHeight();
        camera.update();

        // Render the world
        worldRenderer.render(camera, Gdx.graphics.getDeltaTime());

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

    @Override
    public void update(float deltaTime) {
        // Update game logic if needed (e.g., camera movement, block updates)
        dynamicSky.update(deltaTime);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height); // Ensure viewport updates
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
        shapeRenderer.dispose();
    }
}
