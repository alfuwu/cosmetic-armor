package com.alfred.cosmeticarmor;

import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CosmeticArmor implements ModInitializer {
	public static final String MOD_ID = "cosmetic-armor";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			CosmeticArmorInventory oldInv = oldPlayer.getCosmeticArmor();
			CosmeticArmorInventory newInv = newPlayer.getCosmeticArmor();
			newInv.copyFrom(oldInv);
		});

		ServerPlayerEvents.JOIN.register(joining -> {
			for (ServerPlayerEntity other : joining.server.getPlayerManager().getPlayerList()) {
				if (other == joining)
					continue;

				CosmeticArmorInventory inv = other.getCosmeticArmor();

				SyncCosmeticsS2CPayload payload = new SyncCosmeticsS2CPayload(other.getId(), inv.zip(), inv.getVisibilities());
				ServerPlayNetworking.send(joining, payload);
			}
			joining.getCosmeticArmor().syncToTrackingAndSelf(joining);
		});

		PayloadTypeRegistry.playS2C().register(SyncCosmeticsS2CPayload.ID, SyncCosmeticsS2CPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ToggleVisibilityC2SPayload.ID, ToggleVisibilityC2SPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ToggleVisibilityC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				MinecraftServer world = context.server();

				if (world == null)
					return;

				ServerPlayerEntity entity = context.player();

				CosmeticArmorInventory inv = entity.getCosmeticArmor();
				inv.setVisible(payload.idx(), payload.visible());
				inv.syncToTrackingAndSelf(entity);
			});
		});
	}
}