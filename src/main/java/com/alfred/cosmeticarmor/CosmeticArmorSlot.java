package com.alfred.cosmeticarmor;

import com.alfred.cosmeticarmor.interfaces.CosmeticEditableHandler;
import com.alfred.cosmeticarmor.mixin.SlotAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class CosmeticArmorSlot extends Slot {
    private final CosmeticEditableHandler handler;

    public CosmeticArmorSlot(CosmeticEditableHandler handler, Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.handler = handler;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return handler.isEditingCosmetics() && stack.getItem() instanceof ArmorItem armor && armor.equipmentSlot == ((SlotAccessor)this).getIndex();
    }

    @Override
    public ItemStack takeStack(int amount) {
        if (handler.isEditingCosmetics())
            return super.takeStack(amount);
        return null;
    }

    @Override
    public ItemStack getStack() {
        if (handler.isEditingCosmetics())
            return super.getStack();
        return null;
    }

    @Override
    public boolean hasStack() {
        return handler.isEditingCosmetics() && super.hasStack();
    }
}