package inferno.cube_game.common.items;

import inferno.cube_game.common.registries.ItemRegistry;

public class ItemStack {
    private Item item;
    private int amount;

    public ItemStack(Item item, int amount) {
        this.item = item;
        this.amount = amount;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        if (this.amount<=0){
            setItem(ItemRegistry.getItem("air"));
            this.amount=-1;
        }
    }

    public boolean isEmpty(){
        return item == ItemRegistry.getItem("air") || this.amount <= 0;
    }
}
