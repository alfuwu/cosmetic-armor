package com.alfred.cosmeticarmor.mixin.client;

import com.alfred.cosmeticarmor.CosmeticArmorInventory;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

@Mixin(value = ArmorFeatureRenderer.class, priority = 650)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    @Shadow protected abstract void setVisible(A bipedModel, EquipmentSlot slot);
    @Shadow protected abstract boolean usesInnerModel(EquipmentSlot slot);
    @Shadow protected abstract void renderArmorParts(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, A model, int i, Identifier identifier);
    @Shadow protected abstract void renderTrim(RegistryEntry<ArmorMaterial> armorMaterial, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ArmorTrim trim, A model, boolean leggings);
    @Shadow protected abstract void renderGlint(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, A model);

    @Unique
    private List<Supplier<Boolean>> renderList = new LinkedList<>();

    public ArmorFeatureRendererMixin(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private void renderCosmeticArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        if (!(entity instanceof CosmeticalEntity cosmic))
            return;

        CosmeticArmorInventory inv = cosmic.getCosmeticArmor();
        if (!inv.isVisible(armorSlot)) {
            ci.cancel();
            return;
        }
        ItemStack equippedStack = entity.getEquippedStack(armorSlot);
        ItemStack cosmeticStack = inv.getStack(armorSlot);
        if (cosmeticStack.isEmpty())
            return;

        ArmorRenderer renderer = ArmorRendererRegistryImpl.get(cosmeticStack.getItem());

        if (renderer != null) {
            renderList.add(() -> {
                renderer.render(matrices, vertexConsumers, cosmeticStack, entity, armorSlot, light,
                        (BipedEntityModel<LivingEntity>) getContextModel());
                return true;
            });
            ci.cancel();
        } else if (ArmorRendererRegistryImpl.get(equippedStack.getItem()) != null) {
            renderList.add(() -> {
                renderNormalArmor(matrices, vertexConsumers, entity, cosmeticStack, armorSlot, light, model);
                return true;
            });
            ci.cancel();
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At("TAIL"))
    private void renderDelayed(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        renderList.forEach(Supplier::get);
        renderList.clear();
    }

    @Redirect(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack modifyVisibleArmor(LivingEntity entity, EquipmentSlot slot) {
        ItemStack equippedStack = entity.getEquippedStack(slot);
        if (!(entity instanceof CosmeticalEntity cosmic))
            return equippedStack;

        ItemStack cosmeticStack = cosmic.getCosmeticArmor().getStack(slot);
        return cosmeticStack.isEmpty() || cosmeticStack.isOf(Items.ELYTRA) ? equippedStack : cosmeticStack;
    }

    @Unique
    private void renderNormalArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, T entity, ItemStack itemStack, EquipmentSlot armorSlot, int light, A model) {
        Item var9 = itemStack.getItem();
        if (var9 instanceof ArmorItem armorItem) {
            if (armorItem.getSlotType() == armorSlot) {
                getContextModel().copyBipedStateTo(model);
                setVisible(model, armorSlot);
                boolean bl = usesInnerModel(armorSlot);
                ArmorMaterial armorMaterial = armorItem.getMaterial().value();
                int i = itemStack.isIn(ItemTags.DYEABLE) ? ColorHelper.Argb.fullAlpha(DyedColorComponent.getColor(itemStack, -6265536)) : -1;

                for(ArmorMaterial.Layer layer : armorMaterial.layers()) {
                    int j = layer.isDyeable() ? i : -1;
                    renderArmorParts(matrices, vertexConsumers, light, model, j, layer.getTexture(bl));
                }

                ArmorTrim armorTrim = itemStack.get(DataComponentTypes.TRIM);
                if (armorTrim != null)
                    renderTrim(armorItem.getMaterial(), matrices, vertexConsumers, light, armorTrim, model, bl);

                if (itemStack.hasGlint())
                    renderGlint(matrices, vertexConsumers, light, model);
            }
        }
    }
}
