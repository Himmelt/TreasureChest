package org.soraworld.treasure;

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

public class TreasureChest extends VioletPlugin {

    @Nonnull
    protected IIConfig registerConfig(File path) {
        return new Config(path, this);
    }

    protected void registerEvents() {
        if (iconfig instanceof Config) {
            registerEvent(new EventListener((Config) iconfig, this));
        }
    }

    @Nullable
    protected IICommand registerCommand() {
        if (iconfig instanceof Config) {
            return new CommandTreasure(Constant.PLUGIN_ID, Constant.PERM_ADMIN, (Config) iconfig, this);
        }
        return null;
    }

    protected void afterEnable() {

    }

    protected void beforeDisable() {

    }

}
