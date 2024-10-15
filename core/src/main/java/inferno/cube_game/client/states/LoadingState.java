package inferno.cube_game.client.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import inferno.cube_game.Main;
import inferno.cube_game.common.registries.BlockRegistry;
import inferno.cube_game.common.registries.ItemRegistry;

import java.util.List;

public class LoadingState extends GameState {
    private final GlyphLayout initializationStageLayout;
    private final GlyphLayout currentItemLayout;
    private float loadingProgress = 0.0f;
    private float timer = 0f;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    private enum LoadStep {
        PRE_INITIALIZATION,
        REGISTER_BLOCKS,
        REGISTER_ITEMS,
        POST_INITIALIZATION,
        DONE
    }

    private LoadStep currentStep = LoadStep.PRE_INITIALIZATION;
    private int currentBlockIndex = 0;
    private int currentItemIndex = 0;

    private List<String> blockNames;
    private List<String> itemNames;

    public LoadingState(SpriteBatch batch) {
        super(batch);
        initializationStageLayout = new GlyphLayout();
        currentItemLayout = new GlyphLayout();
    }

    @Override
    public void create() {
        super.create();
        font = new BitmapFont();
        font.getData().setScale(3.0f);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void update(float deltaTime) {
        timer += deltaTime;

        switch (currentStep) {
            case PRE_INITIALIZATION:
                if (timer > .5f) {
                    currentStep = LoadStep.REGISTER_BLOCKS;
                    timer = 0f;
                    loadingProgress = 0;
                    BlockRegistry.registerDefaults(); // Call to registerDefaults
                    blockNames = BlockRegistry.getRegisteredBlockNames(); // Get the names after registration
                }
                loadingProgress += 0.05f;
                initializationStageLayout.setText(font, "Pre-Initialization...");
                break;

            case REGISTER_BLOCKS:
                if (currentBlockIndex < blockNames.size() && timer > 0.25f) {
                    String blockName = blockNames.get(currentBlockIndex);
                    initializationStageLayout.setText(font, "Registering Blocks");
                    currentBlockIndex++;
                    loadingProgress += 0.2f; // Increment loading progress
                    timer = 0f; // Reset timer for next block
                } else if (currentBlockIndex >= blockNames.size()) {
                    currentStep = LoadStep.REGISTER_ITEMS;
                    timer = 0f;
                    loadingProgress = 0;
                    currentBlockIndex = 0; // Reset for next phase
                    ItemRegistry.registerDefaults(); // Call to registerDefaults
                    itemNames = ItemRegistry.getRegisteredItemNames(); // Get the names after registration
                }
                break;

            case REGISTER_ITEMS:
                if (currentItemIndex < itemNames.size() && timer > 0.25f) {
                    String itemName = itemNames.get(currentItemIndex);
                    initializationStageLayout.setText(font, "Registering Items");
                    currentItemIndex++;
                    loadingProgress += 0.2f; // Increment loading progress
                    timer = 0f; // Reset timer for next item
                } else if (currentItemIndex >= itemNames.size()) {
                    currentStep = LoadStep.POST_INITIALIZATION;
                    timer = 0f;
                    loadingProgress = 0;
                    currentItemIndex = 0; // Reset for next phase
                }
                break;

            case POST_INITIALIZATION:
                if (timer > .2f) {
                    currentStep = LoadStep.DONE;
                }
                loadingProgress += 0.25f;
                initializationStageLayout.setText(font, "Post-Initialization...");
                break;

            case DONE:
                break;
        }
    }

    @Override
    public void render() {
        if (font == null
            || shapeRenderer == null
            || initializationStageLayout == null
            || currentItemLayout == null
            || batch == null) return;
        if (initializationStageLayout.width == 0
            && currentItemLayout.width == 0) return;
        if (initializationStageLayout.height == 0
            && currentItemLayout.height == 0) return;

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();

        // Display the current initialization stage
        font.draw(batch, initializationStageLayout,
            (camera.viewportWidth - initializationStageLayout.width) / 2f,
            camera.viewportHeight / 2f + 50f);

        // Display the current block or item being registered
        String currentItemText;
        if (currentStep == LoadStep.REGISTER_BLOCKS && currentBlockIndex > 0) {
            currentItemText = "Currently Registering block : " + blockNames.get(currentBlockIndex - 1);
            Main.blockModelOven.createOrGetBlockModel(BlockRegistry.getBlock(blockNames.get(currentBlockIndex - 1)));
        } else if (currentStep == LoadStep.REGISTER_ITEMS && currentItemIndex > 0) {
            currentItemText = "Currently Registering item : " + itemNames.get(currentItemIndex - 1);
        } else {
            currentItemText = "";
        }
        currentItemLayout.setText(font, currentItemText);
        font.draw(batch, currentItemLayout,
            (viewport.getWorldWidth() - currentItemLayout.width) / 2f,
            (float) viewport.getWorldHeight() / 2f - 60f);

        batch.end();

        // Draw the loading bar using ShapeRenderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GRAY);

        // Draw background of the loading bar
        float barWidth = Gdx.graphics.getWidth() * 0.6f;
        float barHeight = 20;
        float x = (viewport.getWorldWidth()- barWidth) / 2;
        float y = (viewport.getWorldHeight() - barHeight) / 2 - 10;
        shapeRenderer.rect(x, y, barWidth, barHeight);

        // Draw foreground of the loading bar based on loading progress
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(x, y, barWidth * Math.min(loadingProgress, 1.0f), barHeight);

        shapeRenderer.end();

        if ( currentStep == LoadStep.DONE ) {
            GameStateManager.setState(new GameplayState(batch));
        }
    }

    @Override
    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
    }
}
