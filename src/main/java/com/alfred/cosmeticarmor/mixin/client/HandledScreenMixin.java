package com.alfred.cosmeticarmor.mixin.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    @Shadow protected abstract boolean isPointOverSlot(Slot slot, int x, int y);
    @Shadow public ScreenHandler handler;
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;

    @Inject(method = "getSlotAt", at = @At("HEAD"), cancellable = true)
    protected void getSlotAt(int y, int par2, CallbackInfoReturnable<Slot> cir) {
        // do nothing
        // win
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    protected void mouseClicked(int mouseX, int mouseY, int button, CallbackInfo ci) {

    }

    // hacky exposure of isPointOverSlot
    protected boolean pointOverSlot(Slot slot, int x, int y) {
        return isPointOverSlot(slot, x, y);
    }
}
