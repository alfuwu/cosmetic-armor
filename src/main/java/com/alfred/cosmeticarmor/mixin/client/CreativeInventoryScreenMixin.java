package com.alfred.cosmeticarmor.mixin.client;

import com.alfred.cosmeticarmor.ToggleButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.container.ScreenInventoryCreative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenInventoryCreative.class)
public abstract class CreativeInventoryScreenMixin extends InventoryScreenMixin {
	@Inject(method = "drawGuiContainerBackgroundLayer", at = @At("RETURN"))
	private void drawWidgets(float tickDelta, CallbackInfo ci) {
		toggleButton.render(Minecraft.getMinecraft());
		for (ToggleButtonWidget b : visibilityToggles)
			b.render(Minecraft.getMinecraft());
	}
}
