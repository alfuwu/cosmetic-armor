package com.alfred.cosmeticarmor;

import com.alfred.cosmeticarmor.interfaces.CosmeticEditableHandler;
import com.mojang.datafixers.util.Pair;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class CosmeticArmorSlot extends Slot {
    private final CosmeticEditableHandler handler;
    private final Identifier identifier;

    public CosmeticArmorSlot(CosmeticEditableHandler handler, Inventory inventory, int index, int x, int y, @Nullable Identifier identifier) {
        super(inventory, index, x, y);
        this.handler = handler;
        this.identifier = identifier;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return inventory.isValid(getIndex(), stack);
    }

    @Override
    public boolean isEnabled() {
        return handler.isEditingCosmetics();
    }

    @Override
    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return inventory != null && identifier != null ? Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier) : super.getBackgroundSprite();
    }
}
