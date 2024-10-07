package inferno.cube_game.client.models;

import inferno.cube_game.common.blocks.BlockType;

class VoxelFace {
    public boolean transparent;
    public BlockType type;
    public int side;

    public boolean equals(final VoxelFace face) {
        return face.transparent == this.transparent && face.type == this.type;
    }
}
