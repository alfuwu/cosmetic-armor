package com.alfred.cosmeticarmor.mixin.client;

import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ElytraFeatureRenderer.class)
public abstract class ElytraFeatureRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Redirect(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean renderCosmeticArmor(ItemStack instance, Item item, @Local(argsOnly = true) T entity) {
        if (entity instanceof CosmeticalEntity cosmic && cosmic.getCosmeticArmor().getStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA))
            return true;
        return instance.isOf(item);
    }
}
