package com.alfred.cosmeticarmor;

import com.alfred.cosmeticarmor.events.init.InitListener;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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

public class ToggleVisibilityC2SPacket extends Packet implements ManagedPacket<ToggleVisibilityC2SPacket> {
    public static final PacketType<ToggleVisibilityC2SPacket> TYPE = PacketType.builder(false, true, ToggleVisibilityC2SPacket::new).build();
    public static final Identifier ID = Identifier.of(InitListener.NAMESPACE, "tv");

    public byte slot;
    public boolean visible;

    public ToggleVisibilityC2SPacket() { }

    @Environment(EnvType.CLIENT)
    public ToggleVisibilityC2SPacket(byte slot, boolean visible) {
        this.slot = slot;
        this.visible = visible;
    }

    @Override
    public @NotNull PacketType<ToggleVisibilityC2SPacket> getType() {
        return TYPE;
    }

    @Override
    public void read(DataInputStream stream) {
        try {
            byte b = stream.readByte();
            visible = (b >> 7) != 0;
            slot = (byte)(b & (byte)0b01111111);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(DataOutputStream stream) {
        try {
            byte b = (byte)(slot & 0b01111111);
            if (visible)
                b |= (byte)0b10000000;
            stream.writeByte(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void apply(NetworkHandler networkHandler) {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER)
            return;
        PlayerEntity player = PlayerHelper.getPlayerFromPacketHandler(networkHandler);
        World world = player.world;

        if (world == null)
            return;

        CosmeticArmorInventory inv = ((CosmeticalEntity)player).getCosmeticArmor();

        if (slot < 0 || slot >= inv.size())
            return;

        inv.setVisible(slot, visible);
        inv.syncToTrackingAndSelf((ServerPlayerEntity)player);
    }

    @Override
    public int size() {
        return 1;
    }
}
