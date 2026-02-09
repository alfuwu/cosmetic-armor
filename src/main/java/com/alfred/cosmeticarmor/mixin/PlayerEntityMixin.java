package com.alfred.cosmeticarmor.mixin;

import com.alfred.cosmeticarmor.CosmeticArmorInventory;
import com.alfred.cosmeticarmor.events.init.InitListener;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements CosmeticalEntity {
    @Unique public CosmeticArmorInventory cosmeticArmorInventory;

    @Unique
    @Override
    public CosmeticArmorInventory getCosmeticArmor() {
        if (cosmeticArmorInventory == null)
            cosmeticArmorInventory = new CosmeticArmorInventory((PlayerEntity)(Object)this);
        return cosmeticArmorInventory;
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void writeCosmeticArmorNbt(NbtCompound nbt, CallbackInfo ci) {
        getCosmeticArmor().writeToNbt(nbt);
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    private void readCosmeticArmorNbt(NbtCompound nbt, CallbackInfo ci) {
        getCosmeticArmor().readFromNbt(nbt);
    }
}