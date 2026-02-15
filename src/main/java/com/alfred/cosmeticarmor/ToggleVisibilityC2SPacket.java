package com.alfred.cosmeticarmor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.net.packet.PacketCustomPayload;

public class ToggleVisibilityC2SPacket extends PacketCustomPayload {
	public static final String ID = "carm:tv";

    @Environment(EnvType.CLIENT)
    public static PacketCustomPayload create(byte slot, boolean visible) {
		return new PacketCustomPayload(ID, new byte[] { (byte)((slot & 0b01111111) | (visible ? 0b10000000 : 0b0)) });
    }
}
