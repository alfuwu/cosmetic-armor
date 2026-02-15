package com.alfred.cosmeticarmor;

import com.mojang.nbt.tags.ByteArrayTag;
import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.packet.Packet;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.world.WorldServer;

import java.util.Arrays;

public class CosmeticArmorInventory implements Container {
    private final Player owner;
    private final ItemStack[] armor;

    public boolean[] visibilities = new boolean[] { true, true, true, true };

    public CosmeticArmorInventory(Player owner) {
        this.owner = owner;
        this.armor = new ItemStack[4]; // 4 slots: helmet, chest, legs, boots
    }

    public ListTag toNbtList() {
        ListTag nbtList = new ListTag();

        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack itemStack = getItem(i);
            CompoundTag pound;
            if (itemStack == null) {
                pound = new CompoundTag();
                pound.putShort("id", (short)-1);
            } else {
                pound = itemStack.writeToNBT(new CompoundTag());
            }
            nbtList.addTag(pound);
        }

        return nbtList;
    }

    public void readNbtList(ListTag list) {
        Arrays.fill(armor, null);

        for (int i = 0; i < list.tagCount(); i++) {
			CompoundTag pound = (CompoundTag)list.tagAt(i);
            if (!pound.containsKey("id") && !pound.containsKey("stationapi:id") || pound.containsKey("id") && pound.getShort("id") < 0)
                setItem(i, null);
            else {
				ItemStack stack = new ItemStack(0, 0, 0, null);
				stack.readFromNBT(pound);
				setItem(i, stack);
			}
        }
    }

    public ByteArrayTag visibilitiesToNbt() {
        byte[] byteArray = new byte[visibilities.length];

        for (int i = 0; i < visibilities.length; i++)
            byteArray[i] = (byte)(visibilities[i] ? 1 : 0);

        return new ByteArrayTag(byteArray);
    }

    public void readVisibilities(byte[] list) {
        visibilities = new boolean[list.length];

        for (int i = 0; i < visibilities.length; i++)
            visibilities[i] = list[i] == 1;

        if (visibilities.length < 4)
            visibilities = new boolean[] { true, true, true, true };
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.armor[slot];
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (this.armor[slot] != null) {
            if (this.armor[slot].stackSize <= amount) {
                ItemStack var4 = this.armor[slot];
                this.armor[slot] = null;
                this.setChanged();
                return var4;
            } else {
                ItemStack var3 = this.armor[slot].splitStack(amount);
                if (this.armor[slot].stackSize == 0)
                    this.armor[slot] = null;

                this.setChanged();
                return var3;
            }
        } else {
            return null;
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.armor[slot] = stack;
        if (stack != null && stack.stackSize > this.getMaxStackSize())
            stack.stackSize = this.getMaxStackSize();

        this.setChanged();
    }

    @Environment(EnvType.SERVER)
    private void syncServer() {
        if (owner.world instanceof WorldServer)
            syncToTrackingAndSelf((PlayerServer)owner);
    }

    public void setVisible(int i, boolean visible) {
        visibilities[i] = visible;
    }

    public boolean isVisible(int i) {
        return visibilities[i];
    }

    public ItemStack[] zip() {
        ItemStack[] arr = new ItemStack[getContainerSize()];
        for (int i = 0; i < arr.length; i++)
            arr[i] = getItem(i);
        return arr;
    }

    public void unzip(ItemStack[] arr) {
        for (int i = 0; i < arr.length; i++)
            setItem(i, arr[i]);
    }

    @Environment(EnvType.SERVER)
    public void syncToTrackingAndSelf(PlayerServer serverOwner) {
        if (serverOwner.playerNetServerHandler == null)
            return;

        // build packet
        Packet payload = SyncCosmeticsS2CPacket.create(serverOwner.id, zip(), visibilities);

        for (PlayerServer player : serverOwner.mcServer.playerList.playerEntities)
            if (player.playerNetServerHandler != null)
                player.playerNetServerHandler.sendPacket(payload);
    }

    @Environment(EnvType.SERVER)
    public void sync(PlayerServer serverOwner, PlayerServer player) {
        if (player.playerNetServerHandler == null)
            return;

        // build packet
        Packet payload = SyncCosmeticsS2CPacket.create(serverOwner.id, zip(), visibilities);

        player.playerNetServerHandler.sendPacket(payload);
    }

    public void readFromNbt(CompoundTag nbt) {
        if (nbt.containsKey("CosmeticArmor"))
            readNbtList(nbt.getList("CosmeticArmor"));
        if (nbt.containsKey("ArmorVisibility"))
            readVisibilities(nbt.getByteArray("ArmorVisibility"));
    }

    public void writeToNbt(CompoundTag nbt) {
        nbt.put("CosmeticArmor", toNbtList());
        nbt.put("ArmorVisibility", visibilitiesToNbt());
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
            syncServer();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

	@Override
	public void sortContainer() {

	}

	@Override
    public int getContainerSize() {
        return 4;
    }

    @Override
    public String getNameTranslationKey() {
        return "cosmetic-armor";
    }
}
