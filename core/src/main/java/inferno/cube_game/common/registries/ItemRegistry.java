package inferno.cube_game.common.registries;

import inferno.cube_game.common.items.Item;
import inferno.cube_game.common.items.ItemBlock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemRegistry {
    private static final Map<String, Item> items = new HashMap<>();

    // Register an item using its unique registry name
    public static void register(Item item) {
        if (items.containsKey(item.getRegistryName())) {
            throw new IllegalArgumentException("Item already registered: " + item.getRegistryName());
        }
        items.put(item.getRegistryName(), item);
    }

    // Get an item by its registry name
    public static Item getItem(String registryName) {
        return items.get(registryName);
    }

    // Example method to register default items
    public static void registerDefaults() {
        // Registering example items
        // Add more default items as needed
    }

    public static List<String> getRegisteredItemNames() {
        return items.keySet().stream().toList();
    }
}

