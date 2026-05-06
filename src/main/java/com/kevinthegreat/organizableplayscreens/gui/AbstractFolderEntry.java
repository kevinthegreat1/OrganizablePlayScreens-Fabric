package com.kevinthegreat.organizableplayscreens.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AbstractFolderEntry<T extends ObjectSelectionList<E>, E extends ObjectSelectionList.Entry<E>> extends AbstractEntry<T, E> {
    /**
     * Gets all entries contained in this folder.
     */
    List<Identifier> getIcons();

    /**
     * Gets the button that moves the selected entry into this folder.
     *
     * @return the button that moves the selected entry into this folder
     */
    Button getButtonMoveInto();

    /**
     * {@inheritDoc}
     */
    @Override
    default void updateScreenButtonStates(Button selectButton, Button editButton, Button deleteButton, @Nullable Button recreateButton) {
        AbstractEntry.super.updateScreenButtonStates(selectButton, editButton, deleteButton, recreateButton);
        selectButton.setMessage(Component.translatable("organizableplayscreens:folder.openFolder"));
        selectButton.active = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void updateButtonStates(E selectedEntry) {
        getButtonMoveInto().active = selectedEntry != null && !(selectedEntry instanceof ServerSelectionList.LANHeader) && selectedEntry != this;
    }

    @Override
    default void render(GuiGraphicsExtractor context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize) {
        AbstractEntry.renderFolderEntry(context, index, y, x, mouseX, mouseY, hovered, tickDelta, name, listSize, getIcons(), getButtonMoveInto());
    }
}
