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
import inferno.cube_game.client.render.CursorAxisRender;
import inferno.cube_game.client.render.DynamicSky;
import inferno.cube_game.client.render.WorldRenderer;

public class GameplayState extends GameState {
    public static final DirectionalLight DIRECTIONAL_LIGHT = new DirectionalLight().set(1f, 1f, 1f,
        0f, -1f, 0f);
    private WorldRenderer worldRenderer;
    private Environment environment;
    private final DynamicSky dynamicSky;
    private CursorAxisRender cursorAxisRender;

    public GameplayState(SpriteBatch batch) {
        super(batch);
        this.dynamicSky = new DynamicSky();
    }

    @Override
    public void create() {
        environment = new Environment();
        environment.set(
            new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f),
            new ColorAttribute(ColorAttribute.Fog, 0.0f, 0.0f, 0.0f, 1f),
            new ColorAttribute(ColorAttribute.Reflection, 0.8f, 0.8f, 0.8f, 1f),
            new ColorAttribute(ColorAttribute.Ambient, 0.8f, 0.8f, 0.8f, 1f)
        );
        environment.add(DIRECTIONAL_LIGHT);

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        camera.position.set(0, 256f, 0); // Position the camera above the blocks
        //camera.lookAt(0f, 0f, 0f); // Look towards the blocks
        camera.near = 0.1f;
        camera.far = 1000f;
        camera.update();

        cursorAxisRender = new CursorAxisRender(camera);

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


        cursorAxisRender.renderAxisLinesForPlayerCursor();

    }

    @Override
    public void update(float deltaTime) {
        // Update game logic if needed (e.g., camera movement, block updates)
        dynamicSky.update(deltaTime);
        if (worldRenderer == null) return;

        worldRenderer.update(deltaTime);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height); // Ensure viewport updates
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
        cursorAxisRender.dispose();
    }
}
