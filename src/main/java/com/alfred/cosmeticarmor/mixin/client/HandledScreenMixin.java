package com.alfred.cosmeticarmor.mixin.client;

import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.container.ScreenContainerAbstract;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ScreenContainerAbstract.class, remap = false)
public abstract class HandledScreenMixin extends Screen {
    @Shadow public MenuAbstract inventorySlots;

	@Shadow
	public int xSize;

	@Shadow
	public int ySize;

	@Shadow
	public abstract boolean getIsMouseOverSlot(Slot slot, int i, int j);

	@Inject(method = "getSlotAtPosition", at = @At("HEAD"), cancellable = true)
    protected void getSlotAt(int y, int par2, CallbackInfoReturnable<Slot> cir) {
        // do nothing
        // win
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    protected void mouseClicked(int mouseX, int mouseY, int button, CallbackInfo ci) {

    }
}
