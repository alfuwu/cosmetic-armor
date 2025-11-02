package com.alfred.cosmeticarmor.mixin.client;

import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HeadFeatureRenderer.class)
public class HeadFeatureRendererMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack modifyVisibleHead(LivingEntity entity, EquipmentSlot slot) {
        ItemStack equippedStack = entity.getEquippedStack(slot);
        if (!(entity instanceof CosmeticalEntity cosmic))
            return equippedStack;

        ItemStack cosmeticStack = cosmic.getCosmeticArmor().getStack(slot);
        return cosmeticStack.isEmpty() ? equippedStack : cosmeticStack;
    }
}