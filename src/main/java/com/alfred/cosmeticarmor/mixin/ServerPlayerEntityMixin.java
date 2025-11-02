package com.alfred.cosmeticarmor.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void writeCosmeticArmorNbt(NbtCompound nbt, CallbackInfo ci) {
        getCosmeticArmor().writeToNbt(nbt, getRegistryManager());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void readCosmeticArmorNbt(NbtCompound nbt, CallbackInfo ci) {
        getCosmeticArmor().readFromNbt(nbt, getRegistryManager());
    }
}
