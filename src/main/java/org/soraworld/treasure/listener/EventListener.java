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
            if (inv != null && inv.getHolder() instanceof DoubleChest) {
                DoubleChest chest = (DoubleChest) inv.getHolder();
                TreasureBox left = config.getTreasure(((Chest) chest.getLeftSide()).getBlock());
                TreasureBox right = config.getTreasure(((Chest) chest.getRightSide()).getBlock());
                if (left != null || right != null) {
                    if (!player.hasPermission("treasure.use")) {
                        event.setCancelled(true);
                        ServerUtils.send(player, LangKeys.format("noPermission", "treasure.use"));
                    } else {
                        if (left != null && right != null) {
                            if (!left.canOpen() || !right.canOpen()) {
                                event.setCancelled(true);
                                ServerUtils.send(player, LangKeys.format("engrossBox"));
                            } else {
                                left.setOpen(true);
                                right.setOpen(true);
                            }
                        } else if (left != null) {
                            if (!left.canOpen()) {
                                event.setCancelled(true);
                                ServerUtils.send(player, LangKeys.format("engrossBox"));
                            } else {
                                left.setOpen(true);
                            }
                        } else {
                            if (!right.canOpen()) {
                                event.setCancelled(true);
                                ServerUtils.send(player, LangKeys.format("engrossBox"));
                            } else {
                                right.setOpen(true);
                            }
                        }
                    }
                }
            } else if (inv != null && inv.getHolder() instanceof Chest) {
                TreasureBox box = config.getTreasure(((Chest) inv.getHolder()).getBlock());
                if (box != null) {
                    if (!player.hasPermission("treasure.use")) {
                        event.setCancelled(true);
                        ServerUtils.send(player, LangKeys.format("noPermission", "treasure.use"));
                    } else {
                        if (!box.canOpen()) {
                            event.setCancelled(true);
                            ServerUtils.send(player, LangKeys.format("engrossBox"));
                        } else {
                            box.setOpen(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void destroyTreasure(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (inv != null && inv.getHolder() instanceof DoubleChest) {
            DoubleChest chest = (DoubleChest) inv.getHolder();
            Block l = ((Chest) chest.getLeftSide()).getBlock();
            Block r = ((Chest) chest.getRightSide()).getBlock();
            // TODO BUG NullPointException ???
            TreasureBox left = config.getTreasure(l);
            TreasureBox right = config.getTreasure(r);

            if (left != null && right != null) {
                left.setOpen(false);
                right.setOpen(false);
                if (left.isDisappear() || right.isDisappear()) {
                    inv.clear();
                    l.setType(Material.AIR);
                    r.setType(Material.AIR);
                }
                TreasureTask.runNewTask(l, left, plugin);
                TreasureTask.runNewTask(r, right, plugin);
            } else if (left != null) {
                left.setOpen(false);
                if (left.isDisappear()) {
                    inv.clear();
                    l.setType(Material.AIR);
                }
                TreasureTask.runNewTask(l, left, plugin);
            } else if (right != null) {
                right.setOpen(false);
                if (right.isDisappear()) {
                    inv.clear();
                    r.setType(Material.AIR);
                }
                TreasureTask.runNewTask(r, right, plugin);
            }
        } else if (inv != null && inv.getHolder() instanceof Chest) {
            Block block = ((Chest) inv.getHolder()).getBlock();
            if (block != null) {
                TreasureBox treasure = config.getTreasure(block);
                if (treasure != null) {
                    treasure.setOpen(false);
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
