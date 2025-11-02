package com.alfred.cosmeticarmor.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// if this mixin fails, another mod is probably doing the same thing it is
// so it should*** be fine
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @ModifyVariable(method = "onCreativeInventoryAction", at = @At("STORE"), ordinal = 1)
    private boolean modifyBoolean(boolean value, @Local(argsOnly = true) CreativeInventoryActionC2SPacket packet) {
        return value || (packet.slot() >= 1 && packet.slot() < this.player.playerScreenHandler.slots.size());
    }
}
