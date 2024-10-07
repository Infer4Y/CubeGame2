package inferno.cube_game.client.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuState extends GameState {
    private Stage stage;
    private Skin skin;
    private TextButton singlePlayerButton;
    private TextButton settingsButton;
    private TextButton exitButton;

    public MainMenuState(SpriteBatch batch) {
        super(batch);
    }

    @Override
    public void create() {
        super.create();
        // Initialize stage and skin (use your own skin or a default one)
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json")); // Use a valid skin file
        skin.getFont("default").getData().setScale(2f);
        skin.getFont("default").getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Create buttons
        singlePlayerButton = new TextButton("Single Player", skin);
        settingsButton = new TextButton("Options", skin);
        exitButton = new TextButton("Exit", skin);

        // Add listeners to buttons
        singlePlayerButton.addListener(event -> {
            if (singlePlayerButton.isPressed()) {
                GameStateManager.setState(new GameplayState(batch));
            }
            return true;
        });

        settingsButton.addListener(event -> {
            if (settingsButton.isPressed()) {
                GameStateManager.setState(new SettingsState(batch)); // Uncomment when implemented
            }
            return true;
        });

        exitButton.addListener(event -> {
            if (exitButton.isPressed()) {
                Gdx.app.exit();
            }
            return true;
        });

        // Layout the buttons using a Table
        Table table = new Table();
        table.setFillParent(true); // Table fills the entire stage
        table.center();


        // Add buttons to the table
        table.add(singlePlayerButton).pad(2).width(200f).height(50f).center().row();
        table.add(settingsButton).fillX().pad(2).height(50f).center().row();
        table.add(exitButton).fillX().pad(2).height(50f).center().row();

        // Add the table to the stage
        stage.addActor(table);
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
        // Let the stage handle input events and updates
        stage.act(deltaTime);
    }

    @Override
    public void dispose() {
        // Dispose of resources
        stage.dispose();
        skin.dispose();
    }
}
