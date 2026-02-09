package com.alfred.cosmeticarmor.interfaces;

import com.alfred.cosmeticarmor.CosmeticArmorInventory;

public interface CosmeticalEntity {
    default CosmeticArmorInventory getCosmeticArmor() {
        return null;
    }
}