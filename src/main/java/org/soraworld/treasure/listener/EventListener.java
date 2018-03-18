package org.soraworld.treasure.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
    public void clickBlock(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block != null && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (player.hasPermission("treasure.admin") && player.getItemInHand().getType() == Material.BLAZE_ROD) {
                event.setCancelled(true);
                config.setSelect(player, block);
                ServerUtils.send(player, LangKeys.format("blockSelected"));
            }
        } else if (block != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (block.getType() == Material.CHEST && config.hasTreasure(block) && !player.hasPermission("treasure.use")) {
                event.setCancelled(true);
                ServerUtils.send(player, LangKeys.format("noPermission", "treasure.use"));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void closeInventory(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (inv.getHolder() instanceof DoubleChest) {
            DoubleChest chest = (DoubleChest) inv.getHolder();
            closeInventory(((Chest) chest.getLeftSide()).getBlock(), ((Chest) chest.getLeftSide()).getBlockInventory());
            closeInventory(((Chest) chest.getRightSide()).getBlock(), ((Chest) chest.getRightSide()).getBlockInventory());
        } else if (inv.getHolder() instanceof Chest) {
            closeInventory(((Chest) inv.getHolder()).getBlock(), inv);
        }
    }

    private void closeInventory(Block block, Inventory inv) {
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
