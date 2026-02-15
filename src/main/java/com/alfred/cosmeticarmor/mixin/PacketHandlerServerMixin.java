package com.alfred.cosmeticarmor.mixin;

import com.alfred.cosmeticarmor.CosmeticArmorInventory;
import com.alfred.cosmeticarmor.ToggleVisibilityC2SPacket;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.core.world.World;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.handler.PacketHandlerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PacketHandlerServer.class, remap = false)
public abstract class PacketHandlerServerMixin {
	@Shadow private PlayerServer playerEntity;

	@Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
	private void handleToggleVisibilityPacket(PacketCustomPayload packet, CallbackInfo ci) {
		if (ToggleVisibilityC2SPacket.ID.equals(packet.channel)) {
			ci.cancel();
			byte b = packet.data[0];
			boolean visible = (b >> 7) != 0;
			byte slot = (byte)(b & (byte)0b01111111);

			World world = playerEntity.world;
			if (world == null)
				return;

			CosmeticArmorInventory inv = ((CosmeticalEntity)playerEntity).getCosmeticArmor();

			if (slot >= inv.getContainerSize())
				return;

			inv.setVisible(slot, visible);
			inv.syncToTrackingAndSelf(playerEntity);
		}
	}
}
