package org.soraworld.treasure.core;

import org.bukkit.Chunk;
import org.bukkit.World;

public class IChunk {

    private final World world;
    private final int x;
    private final int z;

    public IChunk(Chunk chunk) {
        this.world = chunk.getWorld();
        this.x = chunk.getX();
        this.z = chunk.getZ();
    }

    @Override
    public int hashCode() {
        return world.hashCode() + x + z;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof IChunk && this.world == ((IChunk) obj).world && this.x == ((IChunk) obj).x && this.z == ((IChunk) obj).z;
    }

}
