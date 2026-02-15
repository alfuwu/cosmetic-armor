package com.alfred.cosmeticarmor;

import com.alfred.cosmeticarmor.interfaces.CosmeticEditableHandler;
import net.minecraft.core.item.ItemArmor;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.player.inventory.slot.Slot;
import net.minecraft.core.player.inventory.slot.SlotArmor;
import org.jetbrains.annotations.Nullable;

public class CosmeticArmorSlot extends Slot {
    private final CosmeticEditableHandler handler;
	private final int trueIndex;

    public CosmeticArmorSlot(CosmeticEditableHandler handler, Container inventory, int index, int x, int y, int armorIndex) {
        super(inventory, index, x, y);
        this.handler = handler;
		this.trueIndex = armorIndex;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return handler.isEditingCosmetics() && stack.getItem() instanceof ItemArmor armor && armor.getArmorPiece() == this.trueIndex;
    }

    @Override
    public ItemStack remove(int amount) {
        if (handler.isEditingCosmetics())
            return super.remove(amount);
        return null;
    }

    @Override
    public ItemStack getItemStack() {
        if (handler.isEditingCosmetics())
            return super.getItemStack();
        return null;
    }

    @Override
    public boolean hasItem() {
        return handler.isEditingCosmetics() && super.hasItem();
    }

	@Override
	public @Nullable String getItemIcon() {
		return handler.isEditingCosmetics() ? SlotArmor.armorOutlines[this.trueIndex] : null;
	}
}
