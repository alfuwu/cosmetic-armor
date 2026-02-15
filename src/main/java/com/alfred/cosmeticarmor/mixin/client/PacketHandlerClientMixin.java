package com.alfred.cosmeticarmor.mixin.client;

import com.alfred.cosmeticarmor.CosmeticArmorInventory;
import com.alfred.cosmeticarmor.SyncCosmeticsS2CPacket;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.client.net.handler.PacketHandlerClient;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PacketHandlerClient.class, remap = false)
public abstract class PacketHandlerClientMixin {
	@Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
	private void handleSyncCosmeticsPacket(PacketCustomPayload packet, CallbackInfo ci) {
		if (SyncCosmeticsS2CPacket.ID.equals(packet.channel)) {
			ci.cancel();
			PlayerLocal player = Minecraft.getMinecraft().thePlayer;
			World world = player.world;

			if (world == null)
				return;

			int entityId = ((packet.data[0] & 0xFF) << 24) | ((packet.data[1] & 0xFF) << 16) | ((packet.data[2] & 0xFF) << 8) | (packet.data[3] & 0xFF);
			int length = packet.data[4];

			ItemStack[] stacks = new ItemStack[length];
			boolean[] visibilities = new boolean[length];

			int pos = 5;
			for (int i = 0; i < length; i++) {
				int itemId = ((packet.data[pos++] & 0xFF) << 8) | (packet.data[pos++] & 0xFF);

				if (itemId == 0xFFFF) {
					stacks[i] = null;
				} else {
					int stackSize = packet.data[pos++] & 0xFF;
					int metadata = ((packet.data[pos++] & 0xFF) << 8) | (packet.data[pos++] & 0xFF);
					stacks[i] = new ItemStack(itemId, stackSize, metadata);
				}

				visibilities[i] = packet.data[pos++] != 0;
			}

			for (Entity entity : world.getLoadedEntityList()) {
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
	}
}
