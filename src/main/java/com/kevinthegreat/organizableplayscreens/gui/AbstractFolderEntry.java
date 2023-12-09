package com.kevinthegreat.organizableplayscreens.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface AbstractFolderEntry<E extends AlwaysSelectedEntryListWidget.Entry<E>> extends AbstractEntry<E> {
    /**
     * Gets the button that moves the selected entry into this folder.
     *
     * @return the button that moves the selected entry into this folder
     */
    ButtonWidget getButtonMoveInto();

    /**
     * {@inheritDoc}
     */
    @Override
    default void updateScreenButtonStates(ButtonWidget selectButton, ButtonWidget editButton, ButtonWidget deleteButton, @Nullable ButtonWidget recreateButton) {
        AbstractEntry.super.updateScreenButtonStates(selectButton, editButton, deleteButton, recreateButton);
        selectButton.setMessage(Text.translatable("organizableplayscreens:folder.openFolder"));
        selectButton.active = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void updateButtonStates(E selectedEntry) {
        getButtonMoveInto().active = selectedEntry != null && selectedEntry != this;
    }

    @Override
    default void render(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize) {
        AbstractEntry.renderFolderEntry(context, index, y, x, mouseX, mouseY, hovered, tickDelta, name, listSize, getButtonMoveInto());
    }
}
