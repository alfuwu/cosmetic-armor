package com.alfred.cosmeticarmor;

import net.danygames2014.modmenu.util.DrawingUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawContext;
import net.modificationstation.stationapi.api.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ToggleButtonWidget extends DrawContext {
    private final Identifier textureOff;
    private final Identifier textureOn;
    public boolean toggled;

    public int x, y, width, height;

    private final Consumer<ToggleButtonWidget> onPress;

    public ToggleButtonWidget(int x, int y, int width, int height,
                              Identifier textureOff, Identifier textureOn,
                              Consumer<ToggleButtonWidget> onPress) {
        this.textureOff = textureOff;
        this.textureOn = textureOn;
        this.toggled = false;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.onPress = onPress;
    }

    public void toggle() {
        this.toggled = !this.toggled;
    }

    public void click() {
        onPress.accept(this);
    }

    public void render(Minecraft minecraft) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Identifier texture = toggled ? textureOn : textureOff;
        minecraft.textureManager.bindTexture(minecraft.textureManager.getTextureId("/assets/" + texture.namespace + "/" + texture.path));
        DrawingUtil.drawTexture(x, y, 0, 0, width, height, width, height);
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }
}
