package com.alfred.cosmeticarmor.mixin;

import com.alfred.cosmeticarmor.interfaces.CosmeticEditableHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "net.minecraft.screen.slot.ArmorSlot")
public abstract class ArmorSlotMixin extends Slot implements CosmeticEditableHandler {
    public ArmorSlotMixin(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Unique
    private boolean editingArmor = true;

    @Unique
    @Override
    public boolean isEditingCosmetics() {
        return editingArmor;
    }

    @Unique
    @Override
    public void setEditingMode(boolean editingCosmetics) {
        this.editingArmor = !editingCosmetics;
    }

    @Unique
    @Override
    public void toggleEditingMode() {
        editingArmor = !editingArmor;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && editingArmor;
    }
}
