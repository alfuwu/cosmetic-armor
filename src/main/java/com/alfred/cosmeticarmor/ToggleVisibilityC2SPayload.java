package com.alfred.cosmeticarmor;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ToggleVisibilityC2SPayload(int idx, boolean visible) implements CustomPayload {
    public static final Identifier TOGGLE_VISIBILITY_PAYLOAD_ID = Identifier.of(CosmeticArmor.MOD_ID, "tv");
    public static final CustomPayload.Id<ToggleVisibilityC2SPayload> ID = new CustomPayload.Id<>(TOGGLE_VISIBILITY_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, ToggleVisibilityC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ToggleVisibilityC2SPayload::idx,
            PacketCodecs.BOOL, ToggleVisibilityC2SPayload::visible,
            ToggleVisibilityC2SPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
