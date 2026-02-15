package com.alfred.cosmeticarmor;

import com.alfred.cosmeticarmor.events.init.InitListener;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.entity.player.PlayerHelper;
import net.modificationstation.stationapi.api.network.packet.ManagedPacket;
import net.modificationstation.stationapi.api.network.packet.PacketType;
import net.modificationstation.stationapi.api.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SyncCosmeticsS2CPacket extends Packet implements ManagedPacket<SyncCosmeticsS2CPacket> {
    public static final PacketType<SyncCosmeticsS2CPacket> TYPE = PacketType.builder(true, false, SyncCosmeticsS2CPacket::new).build();
    public static final Identifier ID = Identifier.of(InitListener.NAMESPACE, "sc");

    public int entityId;
    public ItemStack[] stacks;
    public boolean[] visibilities;

    public SyncCosmeticsS2CPacket() { }

    @Environment(EnvType.SERVER)
    public SyncCosmeticsS2CPacket(int id, ItemStack[] zip, boolean[] vis) {
        this.entityId = id;
        this.stacks = zip;
        this.visibilities = vis;
    }

    @Override
    public @NotNull PacketType<SyncCosmeticsS2CPacket> getType() {
        return TYPE;
    }

    @Override
    public void read(DataInputStream stream) {
        try {
            entityId = stream.readInt();
            byte num = stream.readByte();
            stacks = new ItemStack[num];
            visibilities = new boolean[num];

            for (int i = 0; i < num; i++) {
                short id = stream.readShort();
                if (id >= 0) {
                    byte count = stream.readByte();
                    short dmg = stream.readShort();
                    stacks[i] = new ItemStack(id, count, dmg);
                }
                visibilities[i] = stream.readBoolean();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(DataOutputStream stream) {
        try {
            stream.writeInt(entityId);
            stream.writeByte(stacks.length);
            for (int i = 0; i < stacks.length; i++) {
                ItemStack stack = stacks[i];
                if (stack == null) {
                    stream.writeShort(-1);
                } else {
                    stream.writeShort(stack.itemId);
                    stream.writeByte(stack.count);
                    stream.writeShort(stack.getDamage());
                }
                stream.writeBoolean(i < visibilities.length && visibilities[i]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void apply(NetworkHandler networkHandler) {
        PlayerEntity player = PlayerHelper.getPlayerFromPacketHandler(networkHandler);
        World world = player.world;

        if (world == null)
            return;

        for (Entity entity : (List<Entity>)world.getEntities()) {
            if (entity.id == entityId) {
                if (entity instanceof CosmeticalEntity cosmic) {
                    CosmeticArmorInventory inv = cosmic.getCosmeticArmor();
                    inv.unzip(stacks);
                    inv.visibilities = visibilities;
                }
                break;
            }
        }
    }

    @Override
    public int size() {
        int i = 5;
        for (ItemStack stack : stacks)
            i += stack == null ? 2 : 5;
        return i;
    }
}