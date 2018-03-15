package org.soraworld.treasure.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.soraworld.treasure.config.Config;
import org.soraworld.treasure.core.TreasureBox;
import org.soraworld.treasure.task.TreasureTask;

public class EventListener implements Listener {

    private final Config config;
    private final Plugin plugin;

    public EventListener(Config config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void destroyTreasure(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (inv != null && inv.getHolder() instanceof Chest) {
            Block block = ((Chest) inv.getHolder()).getBlock();
            if (block != null) {
                TreasureBox treasure = config.getTreasure(block);
                if (treasure != null) {
                    if (treasure.isDisappear()) {
                        inv.clear();
                        block.setType(Material.AIR);
                    }
                    TreasureTask.runNewTask(block, treasure, plugin);
                }
            }
        }
    }

}
