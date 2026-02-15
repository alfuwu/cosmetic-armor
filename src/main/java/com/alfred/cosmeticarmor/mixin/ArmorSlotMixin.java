package com.alfred.cosmeticarmor.mixin;

import com.alfred.cosmeticarmor.interfaces.CosmeticEditableHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.player.inventory.slot.Slot;
import net.minecraft.core.player.inventory.slot.SlotArmor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SlotArmor.class, remap = false)
public abstract class ArmorSlotMixin extends Slot implements CosmeticEditableHandler {
    public ArmorSlotMixin(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Unique private boolean editingArmor = false;

    @Unique
    @Override
    public boolean isEditingCosmetics() {
        return editingArmor && FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER;
    }

    @Unique @Override public void setEditingMode(boolean editingCosmetics) {
        this.editingArmor = editingCosmetics;
    }

    @Unique
    @Override
    public void toggleEditingMode() {
        editingArmor = !editingArmor;
    }

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (isEditingCosmetics())
            cir.setReturnValue(false);
    }

    @Override
    public ItemStack remove(int amount) {
        if (!isEditingCosmetics())
            return super.remove(amount);
        return null;
    }

    @Override
    public ItemStack getItemStack() {
        if (!isEditingCosmetics())
            return super.getItemStack();
        return null;
    }

    @Override
    public boolean hasItem() {
        return !isEditingCosmetics() && super.hasItem();
    }

	@Inject(method = "getItemIcon", at = @At("HEAD"), cancellable = true)
	public void getItemIcon(CallbackInfoReturnable<String> cir) {
		if (isEditingCosmetics())
			cir.setReturnValue(null);
	}
}
