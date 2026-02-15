package com.alfred.cosmeticarmor.mixin;

import com.alfred.cosmeticarmor.CosmeticArmorInventory;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class, remap = false)
public abstract class PlayerEntityMixin implements CosmeticalEntity {
    @Unique public CosmeticArmorInventory cosmeticArmorInventory;

    @Unique
    @Override
    public CosmeticArmorInventory getCosmeticArmor() {
        if (cosmeticArmorInventory == null)
            cosmeticArmorInventory = new CosmeticArmorInventory((Player)(Object)this);
        return cosmeticArmorInventory;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeCosmeticArmorNbt(CompoundTag nbt, CallbackInfo ci) {
        getCosmeticArmor().writeToNbt(nbt);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void readCosmeticArmorNbt(CompoundTag nbt, CallbackInfo ci) {
        getCosmeticArmor().readFromNbt(nbt);
    }
}
