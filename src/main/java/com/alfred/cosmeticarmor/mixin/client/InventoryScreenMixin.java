package com.alfred.cosmeticarmor.mixin.client;

import com.alfred.cosmeticarmor.*;
import com.alfred.cosmeticarmor.interfaces.CosmeticEditableHandler;
import com.alfred.cosmeticarmor.interfaces.CosmeticalEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.client.entity.player.PlayerLocalMultiplayer;
import net.minecraft.client.gui.container.ScreenInventory;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.inventory.slot.Slot;
import net.minecraft.core.util.collection.NamespaceID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ScreenInventory.class, remap = false)
public abstract class InventoryScreenMixin extends HandledScreenMixin {
    @Unique private static final NamespaceID FOCUSED = NamespaceID.getPermanent(CosmeticArmor.MOD_ID, "textures/gui/cosmetic_focused.png");
    @Unique private static final NamespaceID UNFOCUSED = NamespaceID.getPermanent(CosmeticArmor.MOD_ID, "textures/gui/cosmetic_unfocused.png");
    @Unique private static final NamespaceID VISIBLE = NamespaceID.getPermanent(CosmeticArmor.MOD_ID, "textures/gui/visible.png");
    @Unique private static final NamespaceID INVISIBLE = NamespaceID.getPermanent(CosmeticArmor.MOD_ID, "textures/gui/invisible.png");

    @Unique private Player player;

    @Unique private ToggleButtonWidget toggleButton;
    @Unique private final List<ToggleButtonWidget> visibilityToggles = new ArrayList<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ctor(Player player, CallbackInfo ci) {
        this.player = player;
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addToggleButton(CallbackInfo ci) {
        int posX = (this.width - this.xSize) / 2;
        int posY = (this.height - this.ySize) / 2;
        int x = posX + 69; // nice
        int y = posY + 70;

        // 8x7 pixel image button
        toggleButton = new ToggleButtonWidget(
                x, y,
                8, 7,
                FOCUSED,
                UNFOCUSED,
                button -> {
                    ((CosmeticEditableHandler)inventorySlots).toggleEditingMode();
                    button.toggled = ((CosmeticEditableHandler)inventorySlots).isEditingCosmetics();
                }
        );
        toggleButton.toggled = ((CosmeticEditableHandler)inventorySlots).isEditingCosmetics();

        visibilityToggles.clear();

        x = posX + 24;
        y = posY + 6;
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
						if (player instanceof PlayerLocalMultiplayer multi)
							multi.sendQueue.addToSendQueue(ToggleVisibilityC2SPacket.create(finalI, vis));
                    }
            );
            visible.toggled = !inv.isVisible(i);
            visibilityToggles.add(visible);

            y += 18;
        }
    }

    @Inject(method = "drawGuiContainerBackgroundLayer", at = @At("RETURN"))
    private void drawWidgets(float tickDelta, CallbackInfo ci) {
        toggleButton.render(Minecraft.getMinecraft());
        for (ToggleButtonWidget b : visibilityToggles)
            b.render(Minecraft.getMinecraft());
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button, CallbackInfo ci) {
        boolean toggle = toggleButton.isMouseOver(mouseX, mouseY);
        if (toggle || visibilityToggles.stream().anyMatch(v -> v.isMouseOver(mouseX, mouseY))) {
            // holy nesting
            if (button == 0)
                if (toggle)
                    toggleButton.click();
                else
                    for (ToggleButtonWidget b : visibilityToggles)
                        if (b.isMouseOver(mouseX, mouseY))
                            b.click();
            ci.cancel();
        }
    }

    @Override
    protected void getSlotAt(int x, int y, CallbackInfoReturnable<Slot> cir) {
        if (((CosmeticEditableHandler)inventorySlots).isEditingCosmetics())
            for (Slot slot : inventorySlots.slots)
                if (slot instanceof CosmeticArmorSlot && getIsMouseOverSlot(slot, x, y))
                    cir.setReturnValue(slot);
    }
}
