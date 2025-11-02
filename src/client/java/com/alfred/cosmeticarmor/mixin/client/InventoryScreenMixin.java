package com.alfred.cosmeticarmor.mixin.client;

import com.alfred.cosmeticarmor.CosmeticArmor;
import com.alfred.cosmeticarmor.ToggleButtonWidget;
import com.alfred.cosmeticarmor.ToggleVisibilityC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<PlayerScreenHandler> {
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

    public InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ctor(PlayerEntity player, CallbackInfo ci) {
        this.player = player;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addToggleButton(CallbackInfo ci) {
        int x = this.x + 66;
        int y = this.y + 70;

        // 8x7 pixel image button
        toggleButton = new ToggleButtonWidget(
                x, y,
                8, 7,
                FOCUSED,
                UNFOCUSED,
                button -> {
                    handler.toggleEditingMode();
                    ((ToggleButtonWidget) button).setToggled(handler.isEditingCosmetics());
                }
        );
        toggleButton.setToggled(handler.isEditingCosmetics());

        this.addDrawableChild(toggleButton);

        x = this.x + 24;
        y = this.y + 6;
        for (int i = 0; i < 4; i++) {
            int finalI = i;
            ToggleButtonWidget visible = new ToggleButtonWidget(
                    x, y,
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
            visibilityToggles.add(visible);

            this.addDrawableChild(visible);
            y += 18;
        }
    }

    @Inject(method = "method_19891", at = @At("RETURN"))
    private void toggleRecipeBook(ButtonWidget button, CallbackInfo ci) {
        toggleButton.setPosition(this.x + 66, this.y + 70);

        for (int i = 0; i < visibilityToggles.size(); i++)
            visibilityToggles.get(i).setPosition(this.x + 24, this.y + 6 + 18 * i);
    }
}
