package org.soraworld.treasure.core;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TreasureBox {

    private int amount;
    private int refresh;
    private boolean engross;
    private boolean override;
    private boolean broadcast;

    private final Inventory inv;

    public TreasureBox(String title, int amount, int refresh, boolean engross, boolean override, boolean broadcast) {
        this.amount = amount;
        this.refresh = refresh;
        this.engross = engross;
        this.override = override;
        this.broadcast = broadcast;
        this.inv = Bukkit.createInventory(null, 54, title);
        inv.setItem(1, new ItemStack(Material.DIAMOND));
    }

    public int getRefresh() {
        return refresh;
    }

    public void setRefresh(int refresh) {
        this.refresh = refresh;
    }

    public ItemStack getItem(int slot) {
        return inv.getItem(slot);
    }

    public Inventory getInventory() {
        return inv;
    }

}
