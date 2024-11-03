package inferno.cube_game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import inferno.cube_game.client.loaders.BlockModelOven;
import inferno.cube_game.client.loaders.TextureLoader;
import inferno.cube_game.client.models.blocks.BlockModels;
import inferno.cube_game.client.states.GameStateManager;
import inferno.cube_game.client.states.MainMenuState;
import inferno.cube_game.common.blocks.Block;
import inferno.cube_game.common.registries.BlockRegistry;

import java.util.concurrent.atomic.AtomicLong;

/** {@link ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    public static final TextureLoader textureLoader = new TextureLoader();
    public static final BlockModelOven blockModelOven = new BlockModelOven();

    public static BitmapFont font;
    public static GlyphLayout fpsCounter = new GlyphLayout();

    FPSLogger logger = new FPSLogger();
    Thread updateThread;


    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        BlockRegistry.registerDefaults();
        for (String blockNames : BlockRegistry.getRegisteredBlockNames()){
            Block block = BlockRegistry.getBlock(blockNames);
            if (block == null || block.isAir()) continue;
            BlockModels.addBlockModel(block,
                blockModelOven.createOrGetBlockModel(block));
        }

        GameStateManager.batch = new SpriteBatch();
        GameStateManager.setState(new MainMenuState(batch)); // Start with LoadingState

        AtomicLong lastGameStateManagerUpdate = new AtomicLong();

        updateThread = new Thread(() -> {
            while (GameStateManager.running) {
                if (System.currentTimeMillis() - lastGameStateManagerUpdate.get() >= 50) {
                    GameStateManager.update(5f/100f);
                    lastGameStateManagerUpdate.set(System.currentTimeMillis());
                }
            }
        }, "UpdateThread");
    }

    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        //System.out.println(Gdx.graphics.getFramesPerSecond());
        GameStateManager.render();

        //logger.log();

        if (!updateThread.isAlive()) {
            updateThread.start();
        }
    }

    @Override
    public void resize(int width, int height) {
        //super.resize(width, height);
        GameStateManager.resize(width, height);
    }

    @Override
    public void dispose() {
        GameStateManager.dispose();
        // Dispose of any other resources if needed
        blockModelOven.dispose();
        textureLoader.dispose();
        batch.dispose();
        font.dispose();
        try {
            updateThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
