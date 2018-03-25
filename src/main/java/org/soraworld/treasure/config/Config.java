package org.soraworld.treasure.config;

import net.minecraft.server.v1_7_R4.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.soraworld.treasure.core.IChunk;
import org.soraworld.treasure.core.TreasureBox;
import org.soraworld.treasure.task.TreasureTask;
import org.soraworld.treasure.util.ServerUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Config {

    private String lang = "en_us";
    private boolean run_init = true;
    private boolean force_init = false;
    private final File file;
    private final File data;
    private final LangKeys langKeys;
    private final YamlConfiguration config = new YamlConfiguration();

    private final HashMap<Block, TreasureBox> blocks = new HashMap<>();
    private final HashMap<IChunk, List<Block>> chunks = new HashMap<>();
    private final HashMap<Player, Block> selects = new HashMap<>();
    private final HashMap<Player, Inventory> origins = new HashMap<>();
    private static final List<Block> EMPTY_LIST = new ArrayList<>();

    private final Plugin plugin;

    public Config(File path, Plugin plugin) {
        this.file = new File(path, "config.yml");
        this.data = new File(path, "inventory.nbt");
        this.langKeys = new LangKeys(new File(path, "lang"));
        this.plugin = plugin;
    }

    public void load() {
        if (!file.exists()) {
            if (lang == null || lang.isEmpty()) {
                lang = "en_us";
            }
            langKeys.setLang(lang);
            return;
        }
        try {
            config.load(file);
            lang = config.getString("lang");
            if (lang == null || lang.isEmpty()) {
                lang = "en_us";
            }
            langKeys.setLang(lang);
            run_init = config.getBoolean("run_init");
            force_init = config.getBoolean("force_init");
            //blocks.clear();
            clearBlocks();
            NBTTagCompound comp = null;
            try {
                comp = NBTCompressedStreamTools.a(new FileInputStream(data));
            } catch (Throwable ignored) {
                data.createNewFile();
                NBTCompressedStreamTools.a(new NBTTagCompound(), new FileOutputStream(data));
            }
            if (comp == null) comp = new NBTTagCompound();
            ConfigurationSection boxes = config.getConfigurationSection("boxes");
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
                                    addTreasure(block, new TreasureBox(block,
                                            box.getInt("refresh"),
                                            box.getInt("rand_amount"),
                                            box.getInt("line_amount"),
                                            box.getBoolean("override"),
                                            box.getBoolean("disappear"),
                                            box.getBoolean("broadcast"), list)
                                    );
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            ServerUtils.console("config file load exception !!!");
        }
    }

    public void initRun() {
        if (run_init) runAll(force_init);
    }

    public void save() {
        try {
            config.set("lang", lang);
            config.set("run_init", run_init);
            config.set("force_init", force_init);
            NBTTagCompound comp = new NBTTagCompound();
            ConfigurationSection boxes = config.createSection("boxes");
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
            data.delete();
            data.createNewFile();
            NBTCompressedStreamTools.a(comp, new FileOutputStream(data));
            config.save(file);
        } catch (Throwable e) {
            e.printStackTrace();
            ServerUtils.console("config file save exception !!!");
        }
    }

    public void lang(String lang) {
        if (lang != null && !lang.isEmpty()) {
            this.lang = lang;
            langKeys.setLang(lang);
        }
    }

    public String lang() {
        return this.lang;
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

    private void addChunkBlock(Block block) {
        Chunk chunk = block.getChunk();
        if (chunk != null) {
            System.out.println("addChunkBlock:" + block + "|" + chunk);
            List<Block> list = chunks.get(new IChunk(chunk));
            if (list == null) {
                list = new ArrayList<>();
                chunks.put(new IChunk(chunk), list);
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
        addTreasure(block, new TreasureBox(block, 10, 5, 6, true, true, false, new NBTTagList()));
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

    public void runChunk(Chunk chunk, boolean force) {
        System.out.println("run chunk:" + chunk + "|" + chunks.containsKey(new IChunk(chunk)));
        if (chunk != null && chunks.containsKey(new IChunk(chunk))) {
            for (Block block : chunks.get(new IChunk(chunk))) {
                runBlock(block, force);
            }
        }
    }

    public void runAll(boolean force) {
        stopAll(false);
        for (Block block : blocks.keySet()) {
            runBlock(block, force);
        }
    }

    private void runBlock(@Nonnull Block block, boolean force) {
        byte meta = block.getData();
        if (force) block.setType(Material.CHEST);
        block.setData(meta);
        TreasureBox box = blocks.get(block);
        TreasureTask.runNewTask(block, box, plugin, true);
    }

    public void stopAll(boolean force) {
        TreasureTask.stopAll(plugin);
        /*if (force) {
            try {
                for (Block block : blocks.keySet()) {
                    Chunk chunk = block.getChunk();

                    BlockState state = block.getState();
                    if (state instanceof Chest) {
                        Inventory inv = ((Chest) state).getBlockInventory();
                        if (inv != null) inv.clear();
                        //byte meta = block.getData();
                        //block.setType(Material.AIR);
                        //block.setData(meta);
                    }
                }
            } catch (Throwable ignored) {
                ServerUtils.console("Config.stopAll::inv errors");
            }
        }*/
    }

    public void setCopy(Player player, final Inventory src) {
        origins.put(player, src);
    }

    public Inventory getCopy(Player player) {
        return origins.get(player);
    }

    public List<Block> getChunkBlocks(Chunk chunk) {
        if (chunk != null) {
            List<Block> list = chunks.get(new IChunk(chunk));
            if (list != null) return list;
            return EMPTY_LIST;
        }
        return EMPTY_LIST;
    }

}
