package org.soraworld.treasure.task;

import org.bukkit.scheduler.BukkitRunnable;
import org.soraworld.treasure.config.Config;
import org.soraworld.treasure.util.Vec3i;

public class SpawnChestTask extends BukkitRunnable {

    private final Vec3i loc;
    private final Config config;

    public SpawnChestTask(Vec3i loc, Config config) {
        this.loc = loc;
        this.config = config;
    }

    @Override
    public void run() {

    }

}
