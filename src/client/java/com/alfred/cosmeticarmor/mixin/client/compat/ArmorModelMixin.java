package com.alfred.cosmeticarmor.mixin.client.compat;

import com.alfred.cosmeticarmor.CosmeticArmorInventory;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "org.dawnoftime.armoroftheages.client.models.ArmorModel", remap = false)
public abstract class ArmorModelMixin<T extends LivingEntity> extends BipedEntityModel<T> {
    public ArmorModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("RETURN"))
    public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof CosmeticalEntity cosmic))
            return;

        CosmeticArmorInventory inv = cosmic.getCosmeticArmor();
        boolean hv = inv.getStack(EquipmentSlot.HEAD).isEmpty() && inv.isVisible(0);
        boolean cv = inv.getStack(EquipmentSlot.CHEST).isEmpty() && inv.isVisible(1);
        boolean lv = inv.getStack(EquipmentSlot.LEGS).isEmpty() && inv.isVisible(2);
        boolean fv = inv.getStack(EquipmentSlot.FEET).isEmpty() && inv.isVisible(3);
        head.visible = hv;
        hat.visible = hv;
        body.visible = cv;
        rightArm.visible = cv;
        leftArm.visible = cv;
        rightLeg.visible = lv && fv;
        leftLeg.visible = lv && fv;
    }
}
