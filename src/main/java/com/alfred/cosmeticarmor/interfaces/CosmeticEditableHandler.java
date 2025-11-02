package com.alfred.cosmeticarmor.interfaces;

public interface CosmeticEditableHandler {
    default boolean isEditingCosmetics() {
        return false;
    }

    default void toggleEditingMode() { }

    default void setEditingMode(boolean editingCosmetics) { }
}
