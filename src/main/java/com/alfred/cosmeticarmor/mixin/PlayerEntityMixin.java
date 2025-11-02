package com.alfred.cosmeticarmor.mixin;

import com.alfred.cosmeticarmor.CosmeticArmorInventory;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements CosmeticalEntity {
    @Unique
    public CosmeticArmorInventory cosmeticArmorInventory;

    @Unique
    @Override
    public CosmeticArmorInventory getCosmeticArmor() {
        if (cosmeticArmorInventory == null)
            cosmeticArmorInventory = new CosmeticArmorInventory((PlayerEntity) (Object) this);
        return cosmeticArmorInventory;
    }
}
