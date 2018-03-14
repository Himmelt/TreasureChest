package org.soraworld.treasure.task;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.soraworld.treasure.config.Config;
import org.soraworld.treasure.core.TreasureBox;

public class TreasureTask extends BukkitRunnable {

    private final Block block;
    private final TreasureBox box;
    private final byte meta;

    public TreasureTask(Block block, TreasureBox box) {
        this.block = block;
        this.box = box;
        this.meta = block.getData();
    }

    @Override
    public void run() {
        block.setType(Material.CHEST);
        block.setData(meta);
        BlockState state = block.getState();
        if (state instanceof Chest) {
            Chest chest = (Chest) state;
            Inventory inv = chest.getBlockInventory();
            inv.clear();
            inv.setItem(1, box.getItem(1).clone());
        }
    }

    public static void runNewTask(Block block, Config config, Plugin plugin) {
        TreasureBox box = config.getTreasure(block);
        if (box != null) {
            new TreasureTask(block, box).runTaskLater(plugin, box.getRefresh());
        }
    }
}
