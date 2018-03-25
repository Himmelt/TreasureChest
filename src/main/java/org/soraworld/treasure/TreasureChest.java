package org.soraworld.treasure;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.soraworld.treasure.command.CommandTreasure;
import org.soraworld.treasure.command.IICommand;
import org.soraworld.treasure.config.Config;
import org.soraworld.treasure.listener.EventListener;
import org.soraworld.treasure.util.ListUtils;

import java.util.List;

public class TreasureChest extends JavaPlugin {

    private Config config;
    private IICommand command;

    @Override
    public void onEnable() {
        config = new Config(this.getDataFolder(), this);
        config.load();
        //config.initRun();
        config.save();
        this.getServer().getPluginManager().registerEvents(new EventListener(config, this), this);
        command = new CommandTreasure("treasure", this, config);
    }

    @Override
    public void onDisable() {
        config.save();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return command.execute(sender, ListUtils.arrayList(args));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        return command.onTabComplete(sender, cmd, alias, args);
    }

}
