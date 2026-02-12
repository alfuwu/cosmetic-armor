package com.alfred.cosmeticarmor;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Arrays;
import java.util.Collection;

public class CosmeticArmorInventory extends SimpleInventory {
    private final PlayerEntity owner;

    private boolean[] visibilities = new boolean[] { true, true, true, true };

    public CosmeticArmorInventory(PlayerEntity owner) {
        super(4); // 4 slots: helmet, chest, legs, boots
        this.owner = owner;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return switch (slot) {
            case 0 -> owner.getPreferredEquipmentSlot(stack) == EquipmentSlot.HEAD;
            case 1 -> owner.getPreferredEquipmentSlot(stack) == EquipmentSlot.CHEST;
            case 2 -> owner.getPreferredEquipmentSlot(stack) == EquipmentSlot.LEGS;
            case 3 -> owner.getPreferredEquipmentSlot(stack) == EquipmentSlot.FEET;
            default -> false;
        };
    }

    @Override
    public NbtList toNbtList(RegistryWrapper.WrapperLookup registries) {
        NbtList nbtList = new NbtList();

        for (int i = 0; i < size(); ++i) {
            ItemStack itemStack = getStack(i);
            nbtList.add(itemStack.encodeAllowEmpty(registries));
        }

        return nbtList;
    }

    @Override
    public void readNbtList(NbtList list, RegistryWrapper.WrapperLookup registries) {
        clear();

        for (int i = 0; i < list.size(); ++i)
            setStack(i, ItemStack.fromNbtOrEmpty(registries, list.getCompound(i)));
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
    public void setStack(int slot, ItemStack stack) {
        super.setStack(slot, stack);
        if (!owner.getEntityWorld().isClient())
            syncToTrackingAndSelf((ServerPlayerEntity) owner);
    }

    public void setStack(EquipmentSlot slot, ItemStack stack) {
        switch (slot) {
            case HEAD -> setStack(0, stack);
            case CHEST -> setStack(1, stack);
            case LEGS -> setStack(2, stack);
            case FEET -> setStack(3, stack);
            default -> throw new IllegalArgumentException("there is no cosmetic slot for " + slot);
        }
    }

    public void setVisible(int i, boolean visible) {
        visibilities[i] = visible;
    }

    public void setVisible(EquipmentSlot slot, boolean visible) {
        switch (slot) {
            case HEAD -> setVisible(0, visible);
            case CHEST -> setVisible(1, visible);
            case LEGS -> setVisible(2, visible);
            case FEET -> setVisible(3, visible);
            default -> throw new IllegalArgumentException("there is no cosmetic slot for " + slot);
        }
    }

    public boolean isVisible(int i) {
        return visibilities[i];
    }

    public boolean isVisible(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> isVisible(0);
            case CHEST -> isVisible(1);
            case LEGS -> isVisible(2);
            case FEET -> isVisible(3);
            default -> throw new IllegalArgumentException("there is no cosmetic slot for " + slot);
        };
    }

    public void setVisibilities(boolean[] visibilities) {
        this.visibilities = visibilities;
    }

    public boolean[] getVisibilities() {
        return visibilities;
    }

    public ItemStack[] zip() {
        ItemStack[] arr = new ItemStack[size()];
        for (int i = 0; i < arr.length; i++)
            arr[i] = getStack(i);
        return arr;
    }

    public void unzip(ItemStack[] arr) {
        for (int i = 0; i < arr.length; i++)
            super.setStack(i, arr[i]);
    }

    public void syncToTrackingAndSelf(ServerPlayerEntity serverOwner) {
        //StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        //for (StackTraceElement element : stackTrace)
        //    System.out.println(element.toString());

        if (serverOwner.networkHandler == null)
            return;

        // build packet
        SyncCosmeticsS2CPayload payload = new SyncCosmeticsS2CPayload(serverOwner.getId(), zip(), visibilities);

        // send to all players tracking this entity
        Collection<ServerPlayerEntity> tracking = PlayerLookup.tracking(serverOwner);
        for (ServerPlayerEntity player : tracking)
            ServerPlayNetworking.send(player, payload);

        if (!tracking.contains(serverOwner))
            ServerPlayNetworking.send(serverOwner, payload);
    }

    public ItemStack getStack(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> getStack(0);
            case CHEST -> getStack(1);
            case LEGS -> getStack(2);
            case FEET -> getStack(3);
            default -> throw new IllegalArgumentException("there is no cosmetic slot for " + slot);
        };
    }

    public void copyFrom(CosmeticArmorInventory inventory) {
        for (int i = 0; i < size(); i++)
            super.setStack(i, inventory.getStack(i));
        this.visibilities = inventory.visibilities;
        if (!owner.getEntityWorld().isClient())
            syncToTrackingAndSelf((ServerPlayerEntity) owner);
    }

    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        if (nbt.contains("CosmeticArmor"))
            readNbtList(nbt.getList("CosmeticArmor", NbtElement.COMPOUND_TYPE), lookup);
        if (nbt.contains("ArmorVisibility"))
            readVisibilities(nbt.getByteArray("ArmorVisibility"));
    }

    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        nbt.put("CosmeticArmor", toNbtList(lookup));
        nbt.put("ArmorVisibility", visibilitiesToNbt());
    }
}