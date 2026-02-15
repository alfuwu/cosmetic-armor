package com.alfred.cosmeticarmor.mixin;

import com.alfred.cosmeticarmor.CosmeticArmor;
import com.alfred.cosmeticarmor.CosmeticArmorInventory;
import com.alfred.cosmeticarmor.CosmeticArmorSlot;
import com.alfred.cosmeticarmor.interfaces.CosmeticEditableHandler;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.menu.MenuInventory;
import net.minecraft.core.player.inventory.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MenuInventory.class, remap = false)
public abstract class PlayerScreenHandlerMixin extends MenuAbstract implements CosmeticEditableHandler {
    @Unique private Player owner;
    @Unique private boolean editingCosmetics  = false;

    @Inject(method = "<init>(Lnet/minecraft/core/player/inventory/container/ContainerInventory;Z)V", at = @At("RETURN"))
    private void cacheOwner(ContainerInventory inventory, boolean active, CallbackInfo ci) {
        this.owner = inventory.player;
        CosmeticArmorInventory cosmeticInv = ((CosmeticalEntity)this.owner).getCosmeticArmor();
        if (cosmeticInv == null) {
            CosmeticArmor.LOGGER.error("cosmetic armor inventory is null for player {}", this.owner.nickname);
            return;
        }

        for (int i = 0; i < 4; i++)
            addSlot(new CosmeticArmorSlot(this, cosmeticInv, i, 8, 8 + i * 18, 3 - i));
    }

    @Unique
    @Override
    public boolean isEditingCosmetics() {
        return editingCosmetics || FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
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
            if (slot.getContainer() == owner.inventory && slot instanceof CosmeticEditableHandler armorSlot)
                armorSlot.setEditingMode(editingCosmetics);
    }
}
