package inferno.cube_game.client.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class SettingsState extends GameState {
    private Stage stage;
    private Skin skin;

    private TextButton singlePlayerButton;
    private TextButton settingsButton;
    private TextButton exitButton;


    public SettingsState(SpriteBatch batch) {
        super(batch);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void create() {
        super.create();

        skin = new Skin(Gdx.files.internal("ui/uiskin.json")); // Use a valid skin file
        skin.getFont("default").getData().setScale(2f);
        skin.getFont("default").getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Create buttons
        singlePlayerButton = new TextButton("Single Player", skin);
        settingsButton = new TextButton("Options", skin);
        exitButton = new TextButton("Exit", skin);
    }

    @Override
    public void render() {
        camera.viewportWidth = Gdx.graphics.getWidth();
        camera.viewportHeight = Gdx.graphics.getHeight();
        camera.update();
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        batch.begin();

        // Render the stage, which contains the buttons
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        batch.end();
    }

    @Override
    public void update(float deltaTime) {

    }

    @Override
    public void dispose() {
        // Dispose of resources
        stage.dispose();
        skin.dispose();
    }
}
