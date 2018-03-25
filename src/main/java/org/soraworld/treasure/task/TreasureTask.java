package org.soraworld.treasure.task;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.soraworld.treasure.core.TreasureBox;
import org.soraworld.treasure.util.ServerUtils;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class TreasureTask extends BukkitRunnable {

    private final Block block;
    private final TreasureBox box;
    private final byte meta;
    private final Random random = new Random();
    private static final HashSet<Block> running = new HashSet<>();

    private TreasureTask(Block block, @Nonnull TreasureBox box) {
        this.block = block;
        this.box = box;
        this.meta = block.getData();
    }

    @Override
    public void run() {
        running.remove(block);
        if (block.getType() == Material.AIR || block.getType() == Material.CHEST || box.isOverride()) {
            block.setType(Material.CHEST);
            block.setData(meta);
            BlockState state = block.getState();
            if (state instanceof Chest) {
                Chest chest = (Chest) state;
                Inventory inv = chest.getBlockInventory();
                if (inv != null) {
                    try {
                        inv.clear();
                        List<ItemStack> stacks = box.getItems();
                        for (int i = 0; i < box.getRandAmount() && i < inv.getSize(); i++) {
                            ItemStack stack = nextRandItem(stacks);
                            if (stack != null) inv.setItem(i, stack.clone());
                        }
                    } catch (Throwable ignored) {
                        ServerUtils.console("TreasureTask.run::inv errors");
                    }
                }
            }
        }
    }

    private ItemStack nextRandItem(List<ItemStack> stacks) {
        if (stacks.isEmpty()) return null;
        if (stacks.size() == 1) return stacks.get(0);
        return stacks.get(random.nextInt(stacks.size()));
    }

    public static void runNewTask(Block block, TreasureBox box, Plugin plugin, boolean immediate) {
        if (!running.contains(block) && box != null) {
            running.add(block);
            new TreasureTask(block, box).runTaskLater(plugin, immediate ? 1 : box.getRefresh() * 20);
        }
    }

    public static void stopAll(Plugin plugin) {
        Bukkit.getScheduler().cancelTasks(plugin);
        running.clear();
    }

}
