package com.alfred.cosmeticarmor;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.network.codec.PacketCodec;

public record SyncCosmeticsS2CPayload(int entityId, ItemStack[] stacks, boolean[] visibilities) implements CustomPayload {
    public static final Identifier IDENT = Identifier.of("cosmetic-armor", "sync_cosmetics");
    public static final CustomPayload.Id<SyncCosmeticsS2CPayload> ID = new CustomPayload.Id<>(IDENT);

    // PacketCodec using PacketByteBuf - encoder (write) and decoder (read constructor)
    public static final PacketCodec<PacketByteBuf, SyncCosmeticsS2CPayload> CODEC =
            PacketCodec.of(
                    // encoder: write payload to PacketByteBuf
                    (payload, buf) -> {
                        buf.writeInt(payload.entityId());
                        ItemStack[] stacks = payload.stacks();
                        boolean[] visibilities = payload.visibilities();
                        buf.writeInt(stacks.length);
                        for (int i = 0; i < stacks.length; i++) {
                            ItemStack s = stacks[i];
                            ItemStack.OPTIONAL_PACKET_CODEC.encode((RegistryByteBuf) buf, s);
                            buf.writeBoolean(i < visibilities.length && visibilities[i]);
                        }
                    },
                    // decoder: read from PacketByteBuf
                    (buf) -> {
                        int entityId = buf.readInt();
                        int len = buf.readInt();
                        ItemStack[] stacks = new ItemStack[len];
                        boolean[] visibilities = new boolean[len];
                        for (int i = 0; i < len; i++) {
                            stacks[i] = ItemStack.OPTIONAL_PACKET_CODEC.decode((RegistryByteBuf) buf);
                            visibilities[i] = buf.readBoolean();
                        }
                        return new SyncCosmeticsS2CPayload(entityId, stacks, visibilities);
                    }
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
