package inferno.cube_game.client.models.loaders;

import com.badlogic.gdx.files.FileHandle;
import inferno.cube_game.client.models.blocks.BlockModel;

public interface IModelLoader {
    BlockModel loadBlockModel(FileHandle fileHandle);
}
