package com.alfred.cosmeticarmor.compat;

import net.minecraft.item.Item;

public class GeckoBridge {
    private static final boolean LOADED = isLoaded();

    private static boolean isLoaded() {
        try {
            Class.forName("software.bernie.geckolib.animatable.GeoItem");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isInstalled() {
        return LOADED;
    }

    public static boolean isGeoItem(Item item) {
        if (!LOADED) return false;
        try {
            Class<?> geoItemClass = Class.forName("software.bernie.geckolib.animatable.GeoItem");
            return geoItemClass.isInstance(item);
        } catch (Throwable t) {
            return false;
        }
    }

    public static void render(Item item) {
        if (!LOADED) return;
        try {
            Class<?> rendererClass = Class.forName("software.bernie.geckolib.renderer.GeoArmorRenderer");
            // idk
        } catch (Throwable ignored) { }
    }
}
