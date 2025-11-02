package com.alfred.cosmeticarmor.mixin.client;

import com.alfred.cosmeticarmor.CosmeticArmor;
import com.alfred.cosmeticarmor.CosmeticArmorSlot;
import com.alfred.cosmeticarmor.ToggleButtonWidget;
import com.alfred.cosmeticarmor.ToggleVisibilityC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {
    @Shadow public abstract boolean isInventoryTabSelected();

    @Unique
    private static final Identifier FOCUSED = Identifier.of(CosmeticArmor.MOD_ID, "cosmetic_focused");
    @Unique
    private static final Identifier UNFOCUSED = Identifier.of(CosmeticArmor.MOD_ID, "cosmetic_unfocused");
    @Unique
    private static final Identifier VISIBLE = Identifier.of(CosmeticArmor.MOD_ID, "visible");
    @Unique
    private static final Identifier INVISIBLE = Identifier.of(CosmeticArmor.MOD_ID, "invisible");

    @Unique
    private PlayerEntity player;

    @Unique
    private ToggleButtonWidget toggleButton;
    @Unique
    private final List<ToggleButtonWidget> visibilityToggles = new ArrayList<>();

    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ctor(ClientPlayerEntity player, FeatureSet enabledFeatures, boolean operatorTabEnabled, CallbackInfo ci) {
        this.player = player;
    }

    @ModifyArgs(method = "setSelectedTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen$CreativeSlot;<init>(Lnet/minecraft/screen/slot/Slot;III)V"))
    private void modifyAccessorySlotPositions(Args args) {
        Slot slot = args.get(0);
        if (slot instanceof CosmeticArmorSlot) {
            int k = slot.getIndex();
            int l = k / 2;
            int m = k % 2;
            args.set(2, 54 + l * 54);
            args.set(3, 6 + m * 27);
        }
    }

    @Inject(method = "setSelectedTab", at = @At("RETURN"))
    private void showInventoryStuff(ItemGroup group, CallbackInfo ci) {
        boolean isInv = isInventoryTabSelected();
        if (toggleButton != null)
            toggleButton.visible = isInv;
        for (ToggleButtonWidget visible : visibilityToggles)
            visible.visible = isInv;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addToggleButton(CallbackInfo ci) {
        int x = this.x + 96;
        int y = this.y + 40;

        boolean isInv = isInventoryTabSelected();

        // 8x7 pixel image button
        toggleButton = new ToggleButtonWidget(
                x, y,
                8, 7,
                FOCUSED,
                UNFOCUSED,
                button -> {
                    player.playerScreenHandler.toggleEditingMode();
                    ((ToggleButtonWidget) button).setToggled(player.playerScreenHandler.isEditingCosmetics());
                    CosmeticArmor.LOGGER.info("Toggled cosmetic editing: {}", player.playerScreenHandler.isEditingCosmetics());
                }
        );
        toggleButton.setToggled(player.playerScreenHandler.isEditingCosmetics());

        this.addDrawableChild(toggleButton);
        toggleButton.visible = isInv;

        x = this.x + 70;
        y = this.y + 4;
        for (int i = 0; i < 4; i++) {
            int l = i / 2;
            int m = i % 2;
            int finalI = i;
            ToggleButtonWidget visible = new ToggleButtonWidget(
                    x + l * 54, y + m * 27,
                    5, 5,
                    VISIBLE,
                    INVISIBLE,
                    button -> {
                        boolean vis = !player.getCosmeticArmor().isVisible(finalI);
                        player.getCosmeticArmor().setVisible(finalI, vis);
                        ((ToggleButtonWidget) button).setToggled(!vis);
                        ClientPlayNetworking.send(new ToggleVisibilityC2SPayload(finalI, vis));
                    }
            );
            visible.setToggled(!player.getCosmeticArmor().isVisible(i));
            visible.visible = isInv;
            visibilityToggles.add(visible);

            this.addDrawableChild(visible);
        }
    }
}
