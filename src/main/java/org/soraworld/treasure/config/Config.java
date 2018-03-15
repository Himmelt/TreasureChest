package org.soraworld.treasure.config;

import net.minecraft.server.v1_7_R4.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.soraworld.treasure.core.TreasureBox;
import org.soraworld.treasure.util.ServerUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

public class Config {

    private String lang = "en_us";

    private final File file;
    private final File data;
    private final LangKeys langKeys;
    private final YamlConfiguration config = new YamlConfiguration();

    private final HashMap<Block, TreasureBox> blocks = new HashMap<>();

    public Config(File path) {
        this.file = new File(path, "config.yml");
        this.data = new File(path, "inventory.nbt");
        this.langKeys = new LangKeys(new File(path, "lang"));
    }

    public void load() {
        if (!file.exists()) {
            save();
            return;
        }
        try {
            config.load(file);
            lang = config.getString("lang");
            if (lang == null || lang.isEmpty()) {
                lang = "en_us";
            }
            blocks.clear();
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
                    System.out.println(key);
                    ConfigurationSection box = boxes.getConfigurationSection(key);
                    String[] ss = key.split(",");
                    System.out.println("box:" + box);
                    if (box != null && ss.length == 4) {
                        System.out.println(ss[0]);
                        World world = Bukkit.getServer().getWorld(ss[0]);
                        System.out.println("world:" + world);
                        if (world != null) {
                            try {
                                Block block = world.getBlockAt(Integer.valueOf(ss[1]), Integer.valueOf(ss[2]), Integer.valueOf(ss[3]));
                                System.out.println(block);
                                if (block != null) {
                                    System.out.println(block.hashCode());
                                    NBTTagList list = comp.getList(key, 10);
                                    if (list == null) list = new NBTTagList();
                                    blocks.put(block, new TreasureBox(
                                            box.getInt("refresh"),
                                            box.getInt("rand_amount"),
                                            box.getInt("line_amount"),
                                            box.getBoolean("engross"),
                                            box.getBoolean("override"),
                                            box.getBoolean("disappear"),
                                            box.getBoolean("broadcast"), list));
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

        langKeys.setLang(lang);
    }

    public void save() {
        try {
            config.set("lang", lang);

            ConfigurationSection boxes = config.getConfigurationSection("boxes");
            if (boxes != null) {
                for (Block block : blocks.keySet()) {
                    World world = block.getWorld();
                    if (world != null && !world.getName().isEmpty()) {
                        ConfigurationSection box = boxes.getConfigurationSection(world.getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ());
                        if (box != null) {
                            TreasureBox treasure = blocks.get(block);
                            box.set("refresh", treasure.getRefresh());
                            box.set("rand_amount", treasure.getRandAmount());
                            box.set("line_amount", treasure.getLineAmount());
                            box.set("engross", treasure.isEngross());
                            box.set("override", treasure.isOverride());
                            box.set("disappear", treasure.isDisappear());
                            box.set("broadcast", treasure.isBroadcast());
                        }
                    }
                }
            }

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

    public boolean hasTreasure(Block block) {
        return blocks.get(block) != null;
    }

    public TreasureBox getTreasure(Block block) {
        return blocks.get(block);
    }
}
