package org.soraworld.treasure.command;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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
                            config.save();
                        }
                    } else {
                        ServerUtils.send(player, LangKeys.format("notSelect"));
                    }
                    return true;
                }
                return false;
            }
        });
        addSub(new IICommand("delete") {
            @Override
            public boolean execute(CommandSender sender, ArrayList<String> args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Block select = config.getSelect(player);
                    if (config.hasTreasure(select)) {
                        config.deleteTreasure(select);
                        ServerUtils.send(player, LangKeys.format("deleteTreasure"));
                        config.save();
                    } else {
                        ServerUtils.send(player, LangKeys.format("noTreasure"));
                    }
                    return true;
                }
                return false;
            }
        });
        addSub(new IICommand("copy") {
            @Override
            public boolean execute(CommandSender sender, ArrayList<String> args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Block select = config.getSelect(player);
                    if (select != null) {
                        if (config.hasTreasure(select)) {
                            TreasureBox box = config.getTreasure(select);
                            config.setCopy(player, box.getInventory());
                            ServerUtils.send(player, LangKeys.format("copySuccess"));
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
        addSub(new IICommand("paste") {
            @Override
            public boolean execute(CommandSender sender, ArrayList<String> args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Block select = config.getSelect(player);
                    if (select != null) {
                        if (config.hasTreasure(select)) {
                            Inventory copy = config.getCopy(player);
                            if (copy != null) {
                                config.getTreasure(select).pasteInventory(copy);
                                ServerUtils.send(player, LangKeys.format("pasteSuccess"));
                            } else {
                                ServerUtils.send(player, LangKeys.format("noCopy"));
                            }
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
        addSub(new IICommand("open") {
            @Override
            public boolean execute(CommandSender sender, ArrayList<String> args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (args.isEmpty()) {
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
                    } else {
                        String[] ss = args.get(0).split(",");
                        Block block = null;
                        try {
                            if (ss.length == 3) {
                                block = player.getWorld().getBlockAt(Integer.valueOf(ss[0]), Integer.valueOf(ss[1]), Integer.valueOf(ss[2]));
                            } else if (ss.length == 4) {
                                block = Bukkit.getServer().getWorld(ss[0]).getBlockAt(Integer.valueOf(ss[1]), Integer.valueOf(ss[2]), Integer.valueOf(ss[3]));
                            } else {
                                ServerUtils.send(player, LangKeys.format("errorArgs"));
                            }
                        } catch (Throwable ignored) {
                            ServerUtils.send(player, LangKeys.format("errorArgs"));
                        }
                        TreasureBox box = config.getTreasure(block);
                        if (box != null) {
                            player.openInventory(box.getInventory());
                        } else {
                            ServerUtils.send(player, LangKeys.format("noTreasure"));
                        }
                    }

                    return true;
                }
                return false;
            }
        });
        addSub(new IICommand("run") {
            @Override
            public boolean execute(CommandSender sender, ArrayList<String> args) {
                if (args.isEmpty()) config.runAll(false);
                else if (args.get(0).equals("force")) config.runAll(true);
                return true;
            }
        });
        addSub(new IICommand("stop") {
            @Override
            public boolean execute(CommandSender sender, ArrayList<String> args) {
                if (args.isEmpty()) config.stopAll(false);
                else if (args.get(0).equals("force")) config.stopAll(true);
                return true;
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
