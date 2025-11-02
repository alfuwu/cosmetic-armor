package com.alfred.cosmeticarmor.mixin;

import com.alfred.cosmeticarmor.CosmeticArmor;
import com.alfred.cosmeticarmor.CosmeticArmorInventory;
import com.alfred.cosmeticarmor.CosmeticArmorSlot;
import com.alfred.cosmeticarmor.interfaces.CosmeticEditableHandler;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends AbstractRecipeScreenHandler<CraftingRecipeInput, CraftingRecipe> implements CosmeticEditableHandler {
    @Shadow @Final private PlayerEntity owner;

    @Shadow @Final private static EquipmentSlot[] EQUIPMENT_SLOT_ORDER;
    @Shadow @Final private static Map<EquipmentSlot, Identifier> EMPTY_ARMOR_SLOT_TEXTURES;
    @Unique
    private boolean editingCosmetics  = false;

    @Unique
    @Override
    public boolean isEditingCosmetics() {
        return editingCosmetics;
    }

    @Unique
    @Override
    public void setEditingMode(boolean editingCosmetics) {
        this.editingCosmetics = editingCosmetics;
    }

    @Unique
    @Override
    public void toggleEditingMode() {
        editingCosmetics = !editingCosmetics;
        for (Slot slot : this.slots)
            if (slot.inventory == owner.getInventory() && slot.getIndex() >= 36 && slot.getIndex() <= 39 && slot instanceof CosmeticEditableHandler armorSlot)
                armorSlot.setEditingMode(editingCosmetics);
    }

    public PlayerScreenHandlerMixin(ScreenHandlerType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addCosmeticSlots(PlayerInventory inventory, boolean onServer, PlayerEntity player, CallbackInfo ci) {
        CosmeticArmorInventory cosmeticInv = player.getCosmeticArmor();
        if (cosmeticInv == null) {
            CosmeticArmor.LOGGER.error("cosmetic armor inventory is null");
            return;
        }

        for (int i = 0; i < 4; i++) {
            EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_ORDER[i];
            Identifier identifier = EMPTY_ARMOR_SLOT_TEXTURES.get(equipmentSlot);
            addSlot(new CosmeticArmorSlot(this, cosmeticInv, i, 8, 8 + i * 18, identifier));
        }
    }
}
