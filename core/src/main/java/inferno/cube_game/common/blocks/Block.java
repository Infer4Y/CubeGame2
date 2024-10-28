package inferno.cube_game.common.blocks;

import java.util.Objects;

public class Block {
    private String domain; // The namespace or domain of the block
    private String registryName; // The registry name for the block
    private String unlocalizedName; // The unlocalized name for the block
    private boolean solid;
    private boolean air;
    private boolean breakable;
    private float hardness;
    private BlockType type; // Block type for rendering
    private boolean transparent; // Transparency status for rendering

    private Block(Builder builder) {
        this.domain = builder.domain;
        this.registryName = builder.registryName;
        this.unlocalizedName = builder.unlocalizedName;
        this.solid = builder.solid;
        this.air = builder.air;
        this.breakable = builder.breakable;
        this.hardness = builder.hardness;
        this.type = builder.type;
        this.transparent = builder.transparent;
    }

    public String getDomain() {
        return domain;
    }

    public String getRegistryName() {
        return registryName;
    }

    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public boolean isSolid() {
        return solid;
    }

    public boolean isAir() {
        return air;
    }

    public boolean isBreakable() {
        return breakable;
    }

    public float getHardness() {
        return hardness;
    }

    public BlockType getType() {
        return type;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public static class Builder {
        private String domain = "cube_game";
        private String registryName;
        private String unlocalizedName;
        private boolean solid = true; // Default
        private boolean air = false; // Default
        private boolean breakable = true; // Default
        private float hardness = 1.0f; // Default
        private BlockType type = BlockType.DIRT; // Default type
        private boolean transparent = false; // Default transparency

        public Builder setDomain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder setRegistryName(String registryName) {
            this.registryName = registryName;
            return this;
        }

        public Builder setUnlocalizedName(String unlocalizedName) {
            this.unlocalizedName = unlocalizedName;
            return this;
        }

        public Builder setSolid(boolean solid) {
            this.solid = solid;
            return this;
        }

        public Builder setAir(boolean air) {
            this.air = air;
            return this;
        }

        public Builder setBreakable(boolean breakable) {
            this.breakable = breakable;
            return this;
        }

        public Builder setHardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        public Builder setType(BlockType type) {
            this.type = type;
            return this;
        }

        public Builder setTransparent(boolean transparent) {
            this.transparent = transparent;
            return this;
        }

        public Block build() {
            return new Block(this);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, registryName, unlocalizedName);
    }
}
