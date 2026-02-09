package com.alfred.cosmeticarmor.mixin;

import com.alfred.cosmeticarmor.CosmeticArmorInventory;
import com.alfred.cosmeticarmor.CosmeticArmorSlot;
import com.alfred.cosmeticarmor.events.init.InitListener;
import com.alfred.cosmeticarmor.interfaces.CosmeticEditableHandler;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends ScreenHandler implements CosmeticEditableHandler {
    @Unique private PlayerEntity owner;
    @Unique private boolean editingCosmetics  = false;

    @Inject(method = "<init>(Lnet/minecraft/entity/player/PlayerInventory;Z)V", at = @At("RETURN"))
    private void cacheOwner(PlayerInventory inv, boolean isLocal, CallbackInfo ci) {
        this.owner = inv.player;
        CosmeticArmorInventory cosmeticInv = ((CosmeticalEntity)this.owner).getCosmeticArmor();
        if (cosmeticInv == null) {
            InitListener.LOGGER.error("cosmetic armor inventory is null for player {}", this.owner.name);
            return;
        }

        for (int i = 0; i < 4; i++)
            addSlot(new CosmeticArmorSlot(this, cosmeticInv, i, 8, 8 + i * 18));
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
        for (Slot slot : (List<Slot>)this.slots)
            if (((SlotAccessor)slot).getInventory() == owner.inventory && ((SlotAccessor)slot).getIndex() >= 36 && ((SlotAccessor)slot).getIndex() <= 39 && slot instanceof CosmeticEditableHandler armorSlot)
                armorSlot.setEditingMode(editingCosmetics);
    }
}
