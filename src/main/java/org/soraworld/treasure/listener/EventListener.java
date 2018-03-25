package org.soraworld.treasure.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.soraworld.treasure.config.Config;
import org.soraworld.treasure.config.LangKeys;
import org.soraworld.treasure.core.TreasureBox;
import org.soraworld.treasure.task.TreasureTask;
import org.soraworld.treasure.util.ServerUtils;

import java.util.HashMap;

public class EventListener implements Listener {

    private final Config config;
    private final Plugin plugin;
    private final HashMap<Player, Long> clicks = new HashMap<>();

    public EventListener(Config config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void breakBlock(BlockBreakEvent event) {
        if (event.getPlayer() != null && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            leftClick(event, event.getPlayer(), event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void clickBlock(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            leftClick(event, event.getPlayer(), event.getClickedBlock());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            rightClick(event, event.getPlayer(), event.getClickedBlock());
            // normal player open treasure-box
            if (block != null && block.getType() == Material.CHEST && config.hasTreasure(block) && !player.hasPermission("treasure.use")) {
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

    @EventHandler
    public void onChunkLoad(final ChunkLoadEvent event) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (event.getChunk() != null) {
                    config.runChunk(event.getChunk(), true);
                }
            }
        });
    }

    @EventHandler
    public void onSave(WorldSaveEvent event) {
        config.save();
    }

    private void leftClick(Cancellable event, Player player, Block block) {
        if (player != null && block != null && player.getItemInHand() != null && player.hasPermission("treasure.admin")) {
            if (event instanceof BlockBreakEvent) {
                Long last = clicks.get(player);
                if (last != null && System.currentTimeMillis() - last < 300) {
                    event.setCancelled(true);
                    return;
                }
            }
            ItemStack stack = player.getItemInHand();
            if (stack != null && stack.getType() == Material.GOLD_SPADE) {
                // create
                clicks.put(player, System.currentTimeMillis());
                event.setCancelled(true);
                if (config.hasTreasure(block)) {
                    ServerUtils.send(player, LangKeys.format("existTreasure"));
                } else {
                    config.createTreasure(block);
                    ServerUtils.send(player, LangKeys.format("createTreasure"));
                    config.save();
                }
            } else if (stack != null && stack.getType() == Material.GOLD_PICKAXE) {
                // copy
                clicks.put(player, System.currentTimeMillis());
                event.setCancelled(true);
                if (config.hasTreasure(block)) {
                    TreasureBox box = config.getTreasure(block);
                    config.setCopy(player, box.getInventory());
                    ServerUtils.send(player, LangKeys.format("copySuccess"));
                } else {
                    ServerUtils.send(player, LangKeys.format("noTreasure"));
                }
            } else if (stack != null && stack.getType() == Material.GOLD_AXE) {
                // select
                clicks.put(player, System.currentTimeMillis());
                event.setCancelled(true);
                config.setSelect(player, block);
                ServerUtils.send(player, LangKeys.format("blockSelected"));
            }
        }
    }

    private void rightClick(Cancellable event, Player player, Block block) {
        if (player != null && block != null && player.getItemInHand() != null && player.hasPermission("treasure.admin")) {
            ItemStack stack = player.getItemInHand();
            if (stack != null && stack.getType() == Material.GOLD_SPADE) {
                // delete
                event.setCancelled(true);
                if (config.hasTreasure(block)) {
                    config.deleteTreasure(block);
                    ServerUtils.send(player, LangKeys.format("deleteTreasure"));
                    config.save();
                } else {
                    ServerUtils.send(player, LangKeys.format("noTreasure"));
                }
            } else if (stack != null && stack.getType() == Material.GOLD_PICKAXE) {
                // copy
                event.setCancelled(true);
                if (config.hasTreasure(block)) {
                    Inventory copy = config.getCopy(player);
                    if (copy != null) {
                        config.getTreasure(block).pasteInventory(copy);
                        ServerUtils.send(player, LangKeys.format("pasteSuccess"));
                    } else {
                        ServerUtils.send(player, LangKeys.format("noCopy"));
                    }
                } else {
                    ServerUtils.send(player, LangKeys.format("noTreasure"));
                }
            } else if (stack != null && stack.getType() == Material.GOLD_AXE) {
                // open
                event.setCancelled(true);
                TreasureBox box = config.getTreasure(block);
                if (box != null) {
                    player.openInventory(box.getInventory());
                } else {
                    ServerUtils.send(player, LangKeys.format("noTreasure"));
                }
            }
        }
    }

    private void closeInventory(Block block, Inventory inv) {
        if (block != null && inv != null) {
            TreasureBox treasure = config.getTreasure(block);
            if (treasure != null) {
                if (treasure.isDisappear()) {
                    inv.clear();
                    block.setType(Material.AIR);
                }
                TreasureTask.runNewTask(block, treasure, plugin, false);
            }
        }
    }

}
