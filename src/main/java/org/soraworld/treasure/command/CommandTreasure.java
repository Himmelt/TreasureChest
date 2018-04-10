package org.soraworld.treasure.command;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.soraworld.treasure.config.Config;
import org.soraworld.treasure.core.TreasureBox;
import org.soraworld.treasure.task.TreasureTask;
import org.soraworld.violet.command.CommandViolet;
import org.soraworld.violet.command.IICommand;

import java.util.ArrayList;

public class CommandTreasure extends CommandViolet {

    public CommandTreasure(String name, String perm, final Plugin plugin, final Config config) {
        super(name, perm, config, plugin);
        addSub(new IICommand("create", null, config, true) {
            @Override
            public boolean execute(Player player, ArrayList<String> args) {
                Block select = config.getSelect(player);
                if (select != null) {
                    if (config.hasTreasure(select)) {
                        config.send(player, "existTreasure");
                    } else {
                        config.createTreasure(select);
                        config.send(player, "createTreasure");
                        config.save();
                    }
                } else {
                    config.send(player, "notSelect");
                }
                return true;
            }
        });
        addSub(new IICommand("delete", null, config, true) {
            @Override
            public boolean execute(Player player, ArrayList<String> args) {
                Block select = config.getSelect(player);
                if (config.hasTreasure(select)) {
                    config.deleteTreasure(select);
                    config.send(player, "deleteTreasure");
                    config.save();
                } else {
                    config.send(player, "noTreasure");
                }
                return true;
            }
        });
        addSub(new IICommand("copy", null, config, true) {
            @Override
            public boolean execute(Player player, ArrayList<String> args) {
                Block select = config.getSelect(player);
                if (select != null) {
                    if (config.hasTreasure(select)) {
                        TreasureBox box = config.getTreasure(select);
                        config.setCopy(player, box.getInventory());
                        config.send(player, "copySuccess");
                    } else {
                        config.send(player, "noTreasure");
                    }
                } else {
                    config.send(player, "notSelect");
                }
                return true;
            }
        });
        addSub(new IICommand("paste", null, config, true) {
            @Override
            public boolean execute(Player player, ArrayList<String> args) {
                Block select = config.getSelect(player);
                if (select != null) {
                    if (config.hasTreasure(select)) {
                        Inventory copy = config.getCopy(player);
                        if (copy != null) {
                            config.getTreasure(select).pasteInventory(copy);
                            config.send(player, "pasteSuccess");
                        } else {
                            config.send(player, "noCopy");
                        }
                    } else {
                        config.send(player, "noTreasure");
                    }
                } else {
                    config.send(player, "notSelect");
                }
                return true;
            }
        });
        addSub(new IICommand("open", null, config, true) {
            @Override
            public boolean execute(Player player, ArrayList<String> args) {
                if (args.isEmpty()) {
                    Block select = config.getSelect(player);
                    if (select != null) {
                        TreasureBox box = config.getTreasure(select);
                        if (box != null) {
                            player.openInventory(box.getInventory());
                        } else {
                            config.send(player, "noTreasure");
                        }
                    } else {
                        config.send(player, "notSelect");
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
                            config.send(player, "errorArgs");
                        }
                    } catch (Throwable ignored) {
                        config.send(player, "errorArgs");
                    }
                    TreasureBox box = config.getTreasure(block);
                    if (box != null) {
                        player.openInventory(box.getInventory());
                    } else {
                        config.send(player, "noTreasure");
                    }
                }
                return true;
            }
        });
        addSub(new IICommand("run", config) {
            @Override
            public boolean execute(CommandSender sender, ArrayList<String> args) {
                if (args.isEmpty()) config.runAll(false);
                else if (args.get(0).equals("force")) config.runAll(true);
                return true;
            }
        });
        addSub(new IICommand("stop", config) {
            @Override
            public boolean execute(CommandSender sender, ArrayList<String> args) {
                TreasureTask.stopAll(plugin);
                return true;
            }
        });
    }

}
