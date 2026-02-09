package com.alfred.cosmeticarmor.mixin;

import com.alfred.cosmeticarmor.interfaces.CosmeticEditableHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// anonymous armor slot class in PlayerScreenHandler
// mc dev plugin thing doesnt detect it properly for some reason
// whatever
@Mixin(targets = "net.minecraft.class_277$1")
public abstract class ArmorSlotMixin extends Slot implements CosmeticEditableHandler {
    public ArmorSlotMixin(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Unique private boolean editingArmor = false;

    @Unique
    @Override
    public boolean isEditingCosmetics() {
        return editingArmor && FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER;
    }

    @Unique
    @Override
    public void setEditingMode(boolean editingCosmetics) {
        this.editingArmor = editingCosmetics;
    }

    @Unique
    @Override
    public void toggleEditingMode() {
        editingArmor = !editingArmor;
    }

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (isEditingCosmetics())
            cir.setReturnValue(false);
    }

    @Override
    public ItemStack takeStack(int amount) {
        if (!isEditingCosmetics())
            return super.takeStack(amount);
        return null;
    }

    @Override
    public ItemStack getStack() {
        if (!isEditingCosmetics())
            return super.getStack();
        return null;
    }

    @Override
    public boolean hasStack() {
        return !isEditingCosmetics() && super.hasStack();
    }
}
