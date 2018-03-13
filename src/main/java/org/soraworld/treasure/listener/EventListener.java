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
import org.soraworld.treasure.task.SpawnChestTask;
import org.soraworld.treasure.util.Vec3i;

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
            Chest chest = (Chest) inv.getHolder();
            Block block = chest.getBlock();
            if (block != null) {
                Vec3i loc = Vec3i.getVec3i(block);
                if (config.hasLoc(loc)) {
                    block.setType(Material.AIR);
                    new SpawnChestTask(loc, config).runTaskLater(plugin, config.getLocDelay(loc));
                }
            }
        }
    }

}
