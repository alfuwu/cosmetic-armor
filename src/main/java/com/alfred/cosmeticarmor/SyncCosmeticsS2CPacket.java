package com.alfred.cosmeticarmor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.packet.PacketCustomPayload;

public abstract class SyncCosmeticsS2CPacket {
    public static final String ID = "carm:sc";

	@Environment(EnvType.SERVER)
	public static PacketCustomPayload create(int entityId, ItemStack[] stacks, boolean[] visibilities) {
		if (stacks.length > 255)
			throw new IllegalArgumentException("too many item stacks");
		//if (entityId < 0)
		//	throw new IllegalArgumentException("illegal entity id (corrupted?)");
		if (visibilities.length > 255)
			throw new IllegalArgumentException("too many visibility values");
		int size = 5;
		for (ItemStack stack : stacks)
			size += stack == null ? 3 : 6;
		byte[] arr = new byte[size];
		arr[0] = (byte)((entityId >>> 24) & 0xFF);
		arr[1] = (byte)((entityId >>> 16) & 0xFF);
		arr[2] = (byte)((entityId >>> 8) & 0xFF);
		arr[3] = (byte)(entityId & 0xFF);
		arr[4] = (byte)stacks.length;
		int pos = 5;
		for (int i = 0; i < stacks.length; i++) {
			ItemStack stack = stacks[i];
			if (stack == null) {
				arr[pos++] = (byte)0xFF;
				arr[pos++] = (byte)0xFF;
			} else {
				arr[pos++] = (byte)((stack.itemID >>> 8) & 0xFF);
				arr[pos++] = (byte)(stack.itemID & 0xFF);
				arr[pos++] = (byte)(stack.stackSize);
				arr[pos++] = (byte)((stack.getMetadata() >>> 8) & 0xFF);
				arr[pos++] = (byte)(stack.getMetadata() & 0xFF);
			}
			arr[pos++] = (byte)(i < visibilities.length && visibilities[i] ? 1 : 0);
		}
		return new PacketCustomPayload(ID, arr);
	}
}
