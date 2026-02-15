package com.alfred.cosmeticarmor.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public interface CosmeticEditableHandler {
    // we don't sync the isEditingCosmetics flag to the server, so we'll just say that the server can do whatever it wants with these slots
    default boolean isEditingCosmetics() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    default void toggleEditingMode() { }

    default void setEditingMode(boolean editingCosmetics) { }
}