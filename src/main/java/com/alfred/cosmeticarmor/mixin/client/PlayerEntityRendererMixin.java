package com.alfred.cosmeticarmor.mixin.client;

import com.alfred.cosmeticarmor.CosmeticArmorInventory;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.client.render.model.ModelBase;
import net.minecraft.client.render.model.ModelBiped;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.IArmorItem;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemArmor;
import net.minecraft.core.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// we want to go last.
// but subtract 1 in case another mod ABSOLUTELY NEEDS to after us
@Mixin(value = MobRendererPlayer.class, priority = Integer.MAX_VALUE - 1, remap = false)
public abstract class PlayerEntityRendererMixin extends MobRenderer<Player> {
    @Shadow @Final private ModelBiped modelArmor;
    @Shadow @Final private ModelBiped modelArmorChestplate;

    public PlayerEntityRendererMixin(ModelBase entityModel, float shadowRadius) {
        super(entityModel, shadowRadius);
    }

    @Inject(method = "prepareArmor(Lnet/minecraft/core/entity/player/Player;IF)Z", at = @At("HEAD"), cancellable = true)
    private void modifyArmor(Player player, int i, float f, CallbackInfoReturnable<Boolean> cir) {
        CosmeticArmorInventory inv = ((CosmeticalEntity)player).getCosmeticArmor();
        if (!inv.isVisible(i)) {
            cir.setReturnValue(false);
            return;
        }
        ItemStack stack = inv.getItem(i);
        if (stack != null) {
            Item item = stack.getItem();
            if (item instanceof IArmorItem armorItem) {
				if (armorItem.getArmorMaterial() != null) {
					bindTexture(String.format("/assets/%s/textures/armor/%s_%d.png", armorItem.getArmorMaterial().identifier.namespace(), armorItem.getArmorMaterial().identifier.value(), i != 2 ? 1 : 2));
					loadArmor(i);
				}
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private void loadArmor(int i) {
        ModelBiped armor = i == 2 ? this.modelArmor : this.modelArmorChestplate;
        armor.head.visible = i == 0;
        armor.hair.visible = i == 0;
        armor.body.visible = i == 1 || i == 2;
        armor.armRight.visible = i == 1;
        armor.armLeft.visible = i == 1;
        armor.legRight.visible = i == 2 || i == 3;
        armor.legLeft.visible = i == 2 || i == 3;
        this.setArmorModel(armor);
    }
}
