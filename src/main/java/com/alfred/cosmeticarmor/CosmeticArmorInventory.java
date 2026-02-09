package com.alfred.cosmeticarmor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.ServerWorld;
import net.modificationstation.stationapi.api.network.packet.PacketHelper;

import java.util.Arrays;
import java.util.List;

public class CosmeticArmorInventory implements Inventory {
    private final PlayerEntity owner;
    private final ItemStack[] armor;

    public boolean[] visibilities = new boolean[] { true, true, true, true };

    public CosmeticArmorInventory(PlayerEntity owner) {
        this.owner = owner;
        this.armor = new ItemStack[4]; // 4 slots: helmet, chest, legs, boots
    }

    public NbtList toNbtList() {
        NbtList nbtList = new NbtList();

        for (int i = 0; i < size(); i++) {
            ItemStack itemStack = getStack(i);
            NbtCompound pound;
            if (itemStack == null) {
                pound = new NbtCompound();
                pound.putShort("id", (short)-1);
            } else {
                pound = itemStack.writeNbt(new NbtCompound());
            }
            nbtList.add(pound);
        }

        return nbtList;
    }

    public void readNbtList(NbtList list) {
        Arrays.fill(armor, null);

        for (int i = 0; i < list.size(); i++) {
            NbtCompound pound = (NbtCompound)list.get(i);
            if (!pound.contains("id") && !pound.contains("stationapi:id") || pound.contains("id") && pound.getShort("id") < 0)
                setStack(i, null);
            else
                setStack(i, new ItemStack(pound));
        }
    }

    public NbtByteArray visibilitiesToNbt() {
        byte[] byteArray = new byte[visibilities.length];

        for (int i = 0; i < visibilities.length; i++)
            byteArray[i] = (byte)(visibilities[i] ? 1 : 0);

        return new NbtByteArray(byteArray);
    }

    public void readVisibilities(byte[] list) {
        visibilities = new boolean[list.length];

        for (int i = 0; i < visibilities.length; i++)
            visibilities[i] = list[i] == 1;

        if (visibilities.length < 4)
            visibilities = new boolean[] { true, true, true, true };
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.armor[slot];
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (this.armor[slot] != null) {
            if (this.armor[slot].count <= amount) {
                ItemStack var4 = this.armor[slot];
                this.armor[slot] = null;
                this.markDirty();
                return var4;
            } else {
                ItemStack var3 = this.armor[slot].split(amount);
                if (this.armor[slot].count == 0)
                    this.armor[slot] = null;

                this.markDirty();
                return var3;
            }
        } else {
            return null;
        }
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.armor[slot] = stack;
        if (stack != null && stack.count > this.getMaxCountPerStack())
            stack.count = this.getMaxCountPerStack();

        this.markDirty();
    }

    @Environment(EnvType.SERVER)
    private void syncServer() {
        if (owner.world instanceof ServerWorld)
            syncToTrackingAndSelf((ServerPlayerEntity)owner);
    }

    public void setVisible(int i, boolean visible) {
        visibilities[i] = visible;
    }

    public boolean isVisible(int i) {
        return visibilities[i];
    }

    public ItemStack[] zip() {
        ItemStack[] arr = new ItemStack[size()];
        for (int i = 0; i < arr.length; i++)
            arr[i] = getStack(i);
        return arr;
    }

    public void unzip(ItemStack[] arr) {
        for (int i = 0; i < arr.length; i++)
            setStack(i, arr[i]);
    }

    @Environment(EnvType.SERVER)
    public void syncToTrackingAndSelf(ServerPlayerEntity serverOwner) {
        if (serverOwner.networkHandler == null)
            return;

        // build packet
        SyncCosmeticsS2CPacket payload = new SyncCosmeticsS2CPacket(serverOwner.id, zip(), visibilities);

        for (ServerPlayerEntity player : (List<ServerPlayerEntity>)serverOwner.server.playerManager.players)
            if (player.networkHandler != null)
                player.networkHandler.sendPacket(payload);
    }

    @Environment(EnvType.SERVER)
    public void sync(ServerPlayerEntity serverOwner, ServerPlayerEntity player) {
        if (player.networkHandler == null)
            return;

        // build packet
        SyncCosmeticsS2CPacket payload = new SyncCosmeticsS2CPacket(serverOwner.id, zip(), visibilities);

        player.networkHandler.sendPacket(payload);
    }

    public void copyFrom(CosmeticArmorInventory inventory) {
        for (int i = 0; i < size(); i++)
            setStack(i, inventory.getStack(i).copy());
    }

    public void readFromNbt(NbtCompound nbt) {
        if (nbt.contains("CosmeticArmor"))
            readNbtList(nbt.getList("CosmeticArmor"));
        if (nbt.contains("ArmorVisibility"))
            readVisibilities(nbt.getByteArray("ArmorVisibility"));
    }

    public void writeToNbt(NbtCompound nbt) {
        nbt.put("CosmeticArmor", toNbtList());
        nbt.put("ArmorVisibility", visibilitiesToNbt());
    }

    @Override
    public int getMaxCountPerStack() {
        return 64;
    }

    @Override
    public void markDirty() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
            syncServer();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public String getName() {
        return "cosmetic-armor";
    }
}
