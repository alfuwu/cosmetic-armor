package com.alfred.cosmeticarmor;

import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

public class CosmeticArmorClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncCosmeticsS2CPayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				ClientWorld world = context.client().world;

				if (world == null)
					return;

				int entityId = payload.entityId();
				Entity entity = world.getEntityById(entityId);

				if (entity instanceof CosmeticalEntity cosmic) {
					CosmeticArmorInventory inv = cosmic.getCosmeticArmor();
					inv.unzip(payload.stacks());
					inv.setVisibilities(payload.visibilities());
				}
			});
		});
	}
}