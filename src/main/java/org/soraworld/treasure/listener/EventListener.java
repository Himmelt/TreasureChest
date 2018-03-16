package org.soraworld.treasure.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.soraworld.treasure.config.Config;
import org.soraworld.treasure.config.LangKeys;
import org.soraworld.treasure.core.TreasureBox;
import org.soraworld.treasure.task.TreasureTask;
import org.soraworld.treasure.util.ServerUtils;

public class EventListener implements Listener {

    private final Config config;
    private final Plugin plugin;

    public EventListener(Config config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void selectBlock(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player != null && player.hasPermission("treasure.admin") && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() != Material.AIR) {
                config.setSelect(player, block);
                ServerUtils.send(player, LangKeys.format("blockSelected"));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void openTreasure(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            Inventory inv = event.getInventory();
            // TODO double-chest ????
            if (inv != null && inv.getHolder() instanceof Chest) {
                if (config.hasTreasure(((Chest) inv.getHolder()).getBlock())) {
                    if (!player.hasPermission("treasure.use")) {
                        event.setCancelled(true);
                        ServerUtils.send(player, LangKeys.format("noPermission", "treasure.use"));
                    }
                }
            }
        }
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
