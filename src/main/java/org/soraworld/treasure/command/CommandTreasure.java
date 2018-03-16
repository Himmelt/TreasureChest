package org.soraworld.treasure.command;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.soraworld.treasure.config.Config;
import org.soraworld.treasure.config.LangKeys;
import org.soraworld.treasure.core.TreasureBox;
import org.soraworld.treasure.util.ServerUtils;

import java.util.ArrayList;

public class CommandTreasure extends IICommand {

    public CommandTreasure(String name, final Plugin plugin, final Config config) {
        super(name);
        addSub(new IICommand("save") {
            @Override
            public boolean execute(CommandSender sender, ArrayList<String> args) {
                config.save();
                ServerUtils.send(sender, LangKeys.format("configSaved"));
                return true;
            }
        });
        addSub(new IICommand("reload") {
            @Override
            public boolean execute(CommandSender sender, ArrayList<String> args) {
                config.load();
                ServerUtils.send(sender, LangKeys.format("configReloaded"));
                return true;
            }
        });
        addSub(new IICommand("lang") {
            @Override
            public boolean execute(CommandSender sender, ArrayList<String> args) {
                if (args.isEmpty()) {
                    ServerUtils.send(sender, LangKeys.format("language", config.lang()));
                } else {
                    config.lang(args.get(0));
                    ServerUtils.send(sender, LangKeys.format("language", config.lang()));
                }
                return true;
            }
        });
        addSub(new IICommand("create") {
            @Override
            public boolean execute(CommandSender sender, ArrayList<String> args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Block select = config.getSelect(player);
                    if (select != null) {
                        if (config.hasTreasure(select)) {
                            ServerUtils.send(player, LangKeys.format("existTreasure"));
                        } else {
                            config.createTreasure(select);
                            ServerUtils.send(player, LangKeys.format("createTreasure"));
                        }
                    } else {
                        ServerUtils.send(player, LangKeys.format("notSelect"));
                    }
                    return true;
                }
                return false;
            }
        });
        addSub(new IICommand("open") {
            @Override
            public boolean execute(CommandSender sender, ArrayList<String> args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Block select = config.getSelect(player);
                    if (select != null) {
                        TreasureBox box = config.getTreasure(select);
                        if (box != null) {
                            player.openInventory(box.getInventory());
                        } else {
                            ServerUtils.send(player, LangKeys.format("noTreasure"));
                        }
                    } else {
                        ServerUtils.send(player, LangKeys.format("notSelect"));
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (args.size() >= 1) {
            IICommand sub = subs.get(args.remove(0));
            if (sub != null) {
                return sub.execute(sender, args);
            }
        }
        return false;
    }

}
