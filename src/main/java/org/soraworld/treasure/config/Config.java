package org.soraworld.treasure.config;

import net.minecraft.server.v1_7_R4.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.soraworld.treasure.constant.Constant;
import org.soraworld.treasure.core.IChunk;
import org.soraworld.treasure.core.TreasureBox;
import org.soraworld.treasure.task.TreasureTask;
import org.soraworld.violet.config.IIConfig;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Config extends IIConfig {

    private final File nbt_file;
    private final Plugin plugin;
    private final HashMap<Block, TreasureBox> blocks = new HashMap<>();
    private final HashMap<IChunk, List<Block>> chunks = new HashMap<>();
    private final HashMap<Player, Block> selects = new HashMap<>();
    private final HashMap<Player, Inventory> origins = new HashMap<>();

    public Config(File path, Plugin plugin) {
        super(path);
        this.plugin = plugin;
        this.nbt_file = new File(path, "inventory.nbt");
    }

    protected void loadOptions() {
        clearBlocks();
        NBTTagCompound comp = null;
        try {
            comp = NBTCompressedStreamTools.a(new FileInputStream(nbt_file));
        } catch (Throwable e) {
            if (getDebug()) e.printStackTrace();
        }
        if (comp == null) comp = new NBTTagCompound();
        // TODO one world one config: over_world.yml
        ConfigurationSection boxes = getCfgYaml().getConfigurationSection("boxes");
        if (boxes != null) {
            for (String key : boxes.getKeys(false)) {
                ConfigurationSection box = boxes.getConfigurationSection(key);
                String[] ss = key.split(",");
                if (box != null && ss.length == 4) {
                    World world = Bukkit.getServer().getWorld(ss[0]);
                    if (world != null) {
                        try {
                            Block block = world.getBlockAt(Integer.valueOf(ss[1]), Integer.valueOf(ss[2]), Integer.valueOf(ss[3]));
                            if (block != null) {
                                NBTTagList list = comp.getList(key, 10);
                                if (list == null) list = new NBTTagList();
                                addTreasure(block, new TreasureBox(this,
                                        block,
                                        box.getInt("refresh"),
                                        box.getInt("rand_amount"),
                                        box.getInt("line_amount"),
                                        box.getBoolean("override"),
                                        box.getBoolean("disappear"),
                                        box.getBoolean("broadcast"), list)
                                );
                            }
                        } catch (Throwable e) {
                            if (getDebug()) e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    protected void saveOptions() {
        NBTTagCompound comp = new NBTTagCompound();
        ConfigurationSection boxes = getCfgYaml().createSection("boxes");
        if (boxes != null) {
            for (Block block : blocks.keySet()) {
                World world = block.getWorld();
                if (world != null && !world.getName().isEmpty()) {
                    ConfigurationSection box = boxes.createSection(world.getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ());
                    if (box != null) {
                        TreasureBox treasure = blocks.get(block);
                        box.set("refresh", treasure.getRefresh());
                        box.set("rand_amount", treasure.getRandAmount());
                        box.set("line_amount", treasure.getLineAmount());
                        box.set("override", treasure.isOverride());
                        box.set("disappear", treasure.isDisappear());
                        box.set("broadcast", treasure.isBroadcast());
                        comp.set(world.getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ(), treasure.toNBTList());
                    }
                }
            }
        }
        try {
            NBTCompressedStreamTools.a(comp, new FileOutputStream(nbt_file));
        } catch (Throwable e) {
            if (getDebug()) e.printStackTrace();
        }
    }

    public void afterLoad() {

    }

    private void clearBlocks() {
        blocks.clear();
        chunks.clear();
    }

    private void addTreasure(Block block, TreasureBox box) {
        if (block != null && box != null) {
            blocks.put(block, box);
            addChunkBlock(block);
        }
    }

    private void addChunkBlock(@Nonnull Block block) {
        Chunk chunk = block.getChunk();
        if (chunk != null) {
            IChunk iChunk = new IChunk(chunk);
            List<Block> list = chunks.get(iChunk);
            if (list == null) {
                list = new ArrayList<>();
                chunks.put(iChunk, list);
            }
            list.add(block);
        }
    }

    private void delChunkBlock(Block block) {
        Chunk chunk = block.getChunk();
        if (chunk != null) {
            List<Block> list = chunks.get(new IChunk(chunk));
            if (list != null) {
                list.remove(block);
            }
        }
    }

    public boolean hasTreasure(Block block) {
        return block != null && blocks.get(block) != null;
    }

    public void createTreasure(Block block) {
        addTreasure(block, new TreasureBox(this,
                block,
                10,
                5,
                6,
                true,
                true,
                false,
                new NBTTagList()));
    }

    public void deleteTreasure(Block block) {
        blocks.remove(block);
        if (block != null) {
            delChunkBlock(block);
        }
    }

    public TreasureBox getTreasure(Block block) {
        return blocks.get(block);
    }

    public void setSelect(Player player, Block block) {
        selects.put(player, block);
    }

    public Block getSelect(Player player) {
        return selects.get(player);
    }

    public void runChunk(@Nonnull Chunk chunk, boolean force) {
        IChunk iChunk = new IChunk(chunk);
        if (chunks.containsKey(iChunk)) {
            for (Block block : chunks.get(iChunk)) {
                runBlock(block, force);
            }
        }
    }

    public void runAll(boolean force) {
        TreasureTask.stopAll(plugin);
        for (Block block : blocks.keySet()) {
            runBlock(block, force);
        }
    }

    private void runBlock(@Nonnull Block block, boolean force) {
        byte meta = block.getData();
        if (force) block.setType(Material.CHEST);
        block.setData(meta);
        TreasureBox box = blocks.get(block);
        TreasureTask.runNewTask(block, box, plugin, this, true);
    }

    public void setCopy(Player player, final Inventory src) {
        origins.put(player, src);
    }

    public Inventory getCopy(Player player) {
        return origins.get(player);
    }

    @Nonnull
    public String getAdminPerm() {
        return Constant.PERM_ADMIN;
    }

    @Nonnull
    public ChatColor getHeadColor() {
        return ChatColor.YELLOW;
    }

    @Nonnull
    public String getPlainHead() {
        return "[" + Constant.PLUGIN_NAME + "] ";
    }

    public void setPlainHead(String s) {
    }
}
