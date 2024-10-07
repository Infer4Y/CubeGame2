package inferno.cube_game.common.items;

public class Item {
    private final String domain;
    private final String registryName;
    private final String unlocalizedName;

    Item(Builder builder) {
        this.domain = builder.domain;
        this.registryName = builder.registryName;
        this.unlocalizedName = builder.unlocalizedName;
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

    public static class Builder {
        private final String domain;
        private final String registryName;
        private final String unlocalizedName;

        public Builder(String domain, String registryName, String unlocalizedName) {
            this.domain = domain;
            this.registryName = registryName;
            this.unlocalizedName = unlocalizedName;
        }

        public Item build() {
            return new Item(this);
        }
    }
}

