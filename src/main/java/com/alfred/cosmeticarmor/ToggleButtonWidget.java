package com.alfred.cosmeticarmor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.util.collection.NamespaceID;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ToggleButtonWidget {
    private final NamespaceID textureOff;
    private final NamespaceID textureOn;
    public boolean toggled;

    public int x, y, width, height;

    private final Consumer<ToggleButtonWidget> onPress;

    public ToggleButtonWidget(int x, int y, int width, int height,
							  NamespaceID textureOff, NamespaceID textureOn,
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
		NamespaceID texture = toggled ? textureOn : textureOff;
        minecraft.textureManager.bindTexture(minecraft.textureManager.loadTexture("/assets/" + texture.namespace() + "/" + texture.value()));
		Tessellator.instance.startDrawingQuads();
		Tessellator.instance.addVertexWithUV(x, y + height, 0.0, 0.0, 1.0);
		Tessellator.instance.addVertexWithUV(x + width, y + height, 0.0, 1.0, 1.0);
		Tessellator.instance.addVertexWithUV(x + width, y, 0.0, 1.0, 0.0);
		Tessellator.instance.addVertexWithUV(x, y, 0.0, 0.0, 0.0);
		Tessellator.instance.draw();
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }
}
