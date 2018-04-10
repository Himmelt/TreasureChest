package org.soraworld.treasure;

import org.bukkit.event.Listener;
import org.soraworld.treasure.command.CommandTreasure;
import org.soraworld.treasure.config.Config;
import org.soraworld.treasure.constant.Constant;
import org.soraworld.treasure.listener.EventListener;
import org.soraworld.violet.VioletPlugin;
import org.soraworld.violet.command.IICommand;
import org.soraworld.violet.config.IIConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TreasureChest extends VioletPlugin {

    @Nonnull
    protected IIConfig registerConfig(File path) {
        return new Config(path, this);
    }

    @Nonnull
    protected List<Listener> registerEvents(IIConfig iiConfig) {
        ArrayList<Listener> listeners = new ArrayList<>();
        if (config instanceof Config) {
            Config cfg = (Config) config;
            listeners.add(new EventListener(cfg, this));
        }
        return listeners;
    }

    @Nullable
    protected IICommand registerCommand(IIConfig config) {
        if (config instanceof Config) {
            return new CommandTreasure(Constant.PLUGIN_ID, Constant.PERM_ADMIN, this, (Config) config);
        }
        return null;
    }

    protected void afterEnable() {

    }

    protected void beforeDisable() {

    }

}
