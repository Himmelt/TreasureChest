package org.soraworld.treasure.util;

import org.bukkit.block.Block;

public class Vec3i {

    public int x, y, z;

    public Vec3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vec3i getVec3i(Block block) {
        if (block != null) {
            return new Vec3i(block.getX(), block.getY(), block.getZ());
        }
        return null;
    }
}
