package com.alfred.cosmeticarmor.mixin.client;

import com.alfred.cosmeticarmor.CosmeticArmorInventory;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.modificationstation.stationapi.api.registry.RegistryKey;
import net.modificationstation.stationapi.api.template.item.TemplateArmorItem;
import net.modificationstation.stationapi.api.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

// we want to go last.
// but subtract 1 in case another mod ABSOLUTELY NEEDS to after us
@Mixin(value = PlayerEntityRenderer.class, priority = Integer.MAX_VALUE - 1)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer {
    @Shadow @Final private static String[] armorTextureNames;
    @Shadow private BipedEntityModel armor2;
    @Shadow private BipedEntityModel armor1;

    public PlayerEntityRendererMixin(EntityModel entityModel, float shadowRadius) {
        super(entityModel, shadowRadius);
    }

    @Inject(method = "bindTexture(Lnet/minecraft/entity/player/PlayerEntity;IF)Z", at = @At("HEAD"), cancellable = true)
    private void modifyArmor(PlayerEntity player, int i, float f, CallbackInfoReturnable<Boolean> cir) {
        CosmeticArmorInventory inv = ((CosmeticalEntity)player).getCosmeticArmor();
        if (!inv.isVisible(i)) {
            cir.setReturnValue(false);
            return;
        }
        ItemStack stack = inv.getStack(i);
        if (stack != null) {
            Item item = stack.getItem();
            if (item instanceof TemplateArmorItem template) {
                Optional<RegistryKey<Item>> entry = template.getRegistryEntry().getKey();
                if (entry.isPresent()) {
                    Identifier id = entry.get().getRegistry();
                    String prefix = id.path.substring(0, id.path.indexOf('_'));
                    bindTexture("/assets/" + id.namespace + "/textures/armor/" + prefix + "_" + (i == 2 ? 2 : 1) + ".png");
                    loadArmor(i);
                    cir.setReturnValue(true);
                }
            } else if (item instanceof ArmorItem armorItem) {
                bindTexture("/armor/" + armorTextureNames[armorItem.textureIndex] + "_" + (i == 2 ? 2 : 1) + ".png");
                loadArmor(i);
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private void loadArmor(int i) {
        BipedEntityModel armor = i == 2 ? this.armor2 : this.armor1;
        armor.head.visible = i == 0;
        armor.hat.visible = i == 0;
        armor.body.visible = i == 1 || i == 2;
        armor.rightArm.visible = i == 1;
        armor.leftArm.visible = i == 1;
        armor.rightLeg.visible = i == 2 || i == 3;
        armor.leftLeg.visible = i == 2 || i == 3;
        this.setDecorationModel(armor);
    }
}
