package org.soraworld.treasure.config;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.soraworld.treasure.core.TreasureBox;
import org.soraworld.treasure.util.ServerUtils;

import java.io.File;
import java.util.HashMap;

public class Config {

    private String lang = "en_us";

    private final File file;
    private final LangKeys langKeys;
    private final YamlConfiguration config = new YamlConfiguration();

    private final HashMap<Block, TreasureBox> blocks = new HashMap<>();

    private final TreasureBox testBox = new TreasureBox("testBox", 36, 100, false, false, false);

    public Config(File file) {
        this.file = new File(file, "config.yml");
        this.langKeys = new LangKeys(new File(file, "lang"));
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
        } catch (Throwable e) {
            //e.printStackTrace();
            ServerUtils.console("config file load exception !!!");
        }

        langKeys.setLang(lang);
    }

    public void save() {
        try {
            config.set("lang", lang);
            config.save(file);
        } catch (Throwable e) {
            //e.printStackTrace();
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
        return true;
    }

    public TreasureBox getTreasure(Block block) {
        return testBox;
    }
}
