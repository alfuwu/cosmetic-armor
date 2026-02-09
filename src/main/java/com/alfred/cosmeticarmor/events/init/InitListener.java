package com.alfred.cosmeticarmor.events.init;

import com.alfred.cosmeticarmor.SyncCosmeticsS2CPacket;
import com.alfred.cosmeticarmor.ToggleVisibilityC2SPacket;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import com.alfred.cosmeticarmor.mixin.client.MinecraftAccessor;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.modificationstation.stationapi.api.event.mod.InitEvent;
import net.modificationstation.stationapi.api.event.network.packet.PacketRegisterEvent;
import net.modificationstation.stationapi.api.event.tick.GameTickEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import net.modificationstation.stationapi.api.registry.PacketTypeRegistry;
import net.modificationstation.stationapi.api.registry.Registry;
import net.modificationstation.stationapi.api.server.event.network.PlayerLoginEvent;
import net.modificationstation.stationapi.api.util.Namespace;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class InitListener {
    static {
        EntrypointManager.registerLookup(MethodHandles.lookup());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static final Namespace NAMESPACE = Namespace.resolve();

    public static final Logger LOGGER = NAMESPACE.getLogger();

    @EventListener
    private static void serverInit(InitEvent event) {
        // nothin to do
    }

    @EventListener
    public void registerPackets(PacketRegisterEvent event) {
        Registry.register(PacketTypeRegistry.INSTANCE, SyncCosmeticsS2CPacket.ID, SyncCosmeticsS2CPacket.TYPE);
        Registry.register(PacketTypeRegistry.INSTANCE, ToggleVisibilityC2SPacket.ID, ToggleVisibilityC2SPacket.TYPE);
    }

    @EventListener
    public void joinEvent(PlayerLoginEvent event) {
        for (ServerPlayerEntity player : (List<ServerPlayerEntity>)event.player.server.playerManager.players)
            ((CosmeticalEntity)player).getCosmeticArmor().sync(player, event.player);
        ((CosmeticalEntity)event.player).getCosmeticArmor().syncToTrackingAndSelf(event.player);
    }
}
