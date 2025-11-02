package com.alfred.cosmeticarmor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.util.Identifier;

public class ToggleButtonWidget extends TexturedButtonWidget {
    private final Identifier textureOff;
    private final Identifier textureOn;
    private boolean toggled;

    /**
     * Creates a toggleable image button.
     *
     * @param x            X position
     * @param y            Y position
     * @param width        Button width
     * @param height       Button height
     * @param textureOff   Texture shown when toggled = false
     * @param textureOn    Texture shown when toggled = true
     * @param pressAction  Action when clicked
     */
    public ToggleButtonWidget(int x, int y, int width, int height,
                             Identifier textureOff, Identifier textureOn,
                             PressAction pressAction) {
        super(x, y, width, height, new ButtonTextures(textureOff, textureOff), pressAction);
        this.textureOff = textureOff;
        this.textureOn = textureOn;
        this.toggled = false;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public boolean isToggled() {
        return toggled;
    }

    public void toggle() {
        this.toggled = !this.toggled;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Identifier texture = toggled ? textureOn : textureOff;
        context.drawGuiTexture(texture, getX(), getY(), width, height);
    }
}
