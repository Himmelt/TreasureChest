package org.soraworld.treasure.core;

import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.soraworld.treasure.config.Config;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TreasureBox {

    private int refresh;
    private int rand_amount;
    private int line_amount;
    private boolean override;
    private boolean disappear;
    private boolean broadcast;

    private final Inventory inv;

    public TreasureBox(Config config, Block block, int refresh, int rand_amount, int line_amount, boolean override, boolean disappear, boolean broadcast, NBTTagList list) {
        this.refresh = refresh < 0 ? 0 : refresh;

        if (rand_amount < 0) this.rand_amount = 0;
        else if (rand_amount > 27) this.rand_amount = 27;
        else this.rand_amount = rand_amount;

        if (line_amount < 1) this.line_amount = 1;
        else if (line_amount > 6) this.line_amount = 6;
        else this.line_amount = line_amount;

        this.override = override;
        this.disappear = disappear;
        this.broadcast = broadcast;
        this.inv = Bukkit.createInventory(null, 9 * line_amount, getInvName(block, config));
        for (int i = 0; i < list.size(); i++) {
            NBTTagCompound item = list.get(i);
            int slot = item.getByte("slot");
            if (slot >= 0 && slot < 9 * line_amount) {
                inv.setItem(slot, CraftItemStack.asBukkitCopy(net.minecraft.server.v1_7_R4.ItemStack.createStack(item)));
            }
        }
    }

    private String getInvName(Block block, Config config) {
        if (block != null && block.getWorld() != null) {
            return config.formatKey("invName", block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        }
        return "";
    }

    public int getRefresh() {
        return refresh;
    }

    public Inventory getInventory() {
        return inv;
    }

    public int getRandAmount() {
        return rand_amount;
    }

    public int getLineAmount() {
        return line_amount;
    }

    public boolean isOverride() {
        return override;
    }

    public boolean isDisappear() {
        return disappear;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public List<ItemStack> getItems() {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack != null && stack.getType() != Material.AIR && stack.getAmount() > 0) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    public NBTTagList toNBTList() {
        NBTTagList list = new NBTTagList();
        for (byte i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack != null && stack.getType() != Material.AIR) {
                NBTTagCompound item = new NBTTagCompound();
                CraftItemStack.asNMSCopy(stack).save(item);
                item.setByte("slot", i);
                list.add(item);
            }
        }
        return list;
    }

    public void pasteInventory(@Nonnull Inventory src) {
        if (inv == src) return;
        inv.clear();
        for (int i = 0; i < inv.getSize() && i < src.getSize(); i++) {
            inv.setItem(i, src.getItem(i));
        }
    }

}
