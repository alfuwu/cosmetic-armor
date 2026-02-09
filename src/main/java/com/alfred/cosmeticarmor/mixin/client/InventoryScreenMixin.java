package com.alfred.cosmeticarmor.mixin.client;

import com.alfred.cosmeticarmor.CosmeticArmorInventory;
import com.alfred.cosmeticarmor.CosmeticArmorSlot;
import com.alfred.cosmeticarmor.ToggleButtonWidget;
import com.alfred.cosmeticarmor.ToggleVisibilityC2SPacket;
import com.alfred.cosmeticarmor.events.init.InitListener;
import com.alfred.cosmeticarmor.interfaces.CosmeticEditableHandler;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.modificationstation.stationapi.api.network.packet.PacketHelper;
import net.modificationstation.stationapi.api.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreenMixin {
    @Unique private static final Identifier FOCUSED = Identifier.of(InitListener.NAMESPACE, "textures/gui/cosmetic_focused.png");
    @Unique private static final Identifier UNFOCUSED = Identifier.of(InitListener.NAMESPACE, "textures/gui/cosmetic_unfocused.png");
    @Unique private static final Identifier VISIBLE = Identifier.of(InitListener.NAMESPACE, "textures/gui/visible.png");
    @Unique private static final Identifier INVISIBLE = Identifier.of(InitListener.NAMESPACE, "textures/gui/invisible.png");

    @Unique private PlayerEntity player;

    @Unique private ToggleButtonWidget toggleButton;
    @Unique private final List<ToggleButtonWidget> visibilityToggles = new ArrayList<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ctor(PlayerEntity player, CallbackInfo ci) {
        this.player = player;
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addToggleButton(CallbackInfo ci) {
        int x = 69; // nice
        int y = 70;

        // 8x7 pixel image button
        toggleButton = new ToggleButtonWidget(
                x, y,
                8, 7,
                FOCUSED,
                UNFOCUSED,
                button -> {
                    ((CosmeticEditableHandler)handler).toggleEditingMode();
                    button.toggled = ((CosmeticEditableHandler)handler).isEditingCosmetics();
                }
        );
        toggleButton.toggled = ((CosmeticEditableHandler)handler).isEditingCosmetics();

        x = 24;
        y = 6;
        for (byte i = 0; i < 4; i++) {
            byte finalI = i;
            CosmeticArmorInventory inv = ((CosmeticalEntity)player).getCosmeticArmor();
            ToggleButtonWidget visible = new ToggleButtonWidget(
                    x, y,
                    5, 5,
                    VISIBLE,
                    INVISIBLE,
                    button -> {
                        boolean vis = !inv.isVisible(finalI);
                        inv.setVisible(finalI, vis);
                        button.toggled = !vis;
                        PacketHelper.send(new ToggleVisibilityC2SPacket(finalI, vis));
                    }
            );
            visible.toggled = !inv.isVisible(i);
            visibilityToggles.add(visible);

            y += 18;
        }
    }

    @Inject(method = "drawForeground", at = @At("RETURN"))
    private void drawWidgets(CallbackInfo ci) {
        toggleButton.render(MinecraftAccessor.getInstance());
        for (ToggleButtonWidget b : visibilityToggles)
            b.render(MinecraftAccessor.getInstance());
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int x = mouseX - (this.width - this.backgroundWidth) / 2;
        int y = mouseY - (this.height - this.backgroundHeight) / 2;
        boolean toggle = toggleButton.isMouseOver(x, y);
        if (toggle || visibilityToggles.stream().anyMatch(v -> v.isMouseOver(x, y))) {
            // holy nesting
            if (button == 0)
                if (toggle)
                    toggleButton.click();
                else
                    for (ToggleButtonWidget b : visibilityToggles)
                        if (b.isMouseOver(x, y))
                            b.click();
        } else {
            super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    protected void getSlotAt(int x, int y, CallbackInfoReturnable<Slot> cir) {
        if (((CosmeticEditableHandler)handler).isEditingCosmetics())
            for (Slot slot : (List<Slot>)handler.slots)
                if (slot instanceof CosmeticArmorSlot && pointOverSlot(slot, x, y))
                    cir.setReturnValue(slot);
    }
}
