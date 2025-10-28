package com.kevinthegreat.organizableplayscreens.gui;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.TextWidget;

import java.util.List;

@SuppressWarnings("JavadocReference")
public interface MultiplayerServerListWidgetAccessor {
    /**
     * Loads and displays folders and servers from {@code organizable_servers.dat} and the vanilla server list. The vanilla server list stores the server entries while {@code organizable_servers.dat} stores the folder structure.
     */
    default void organizableplayscreens_loadFile() {}

    /**
     * Saves the folders and servers to {@code organizable_servers.dat}.
     */
    default void organizableplayscreens_saveFile() {}

    /**
     * Updates the displayed entries and saves the folders and servers to {@code organizable_servers.dat}.
     */
    default void organizableplayscreens_updateAndSave() {}

    default MultiplayerFolderEntry organizableplayscreens_getCurrentFolder() {return null;}

    default List<MultiplayerServerListWidget.Entry> organizableplayscreens_getCurrentEntries() {return null;}

    default boolean organizableplayscreens_isRootFolder() {return false;}

    default void organizableplayscreens_setPathWidget(TextWidget pathWidget) {}

    /**
     * Sets {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} and updates the displayed entries.
     *
     * @param folderEntry the new folder.
     */
    default void organizableplayscreens_setCurrentFolder(MultiplayerFolderEntry folderEntry) {}

    /**
     * Trys to set {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent and update the displayed entries.
     *
     * @return {@code true} if the folder has a parent and {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} was changed.
     */
    default boolean organizableplayscreens_setCurrentFolderToParent() {return false;}

    /**
     * Swaps the entries at {@code i} and {@code j} and updates and saves the entries.
     *
     * @param i the index of the selected entry
     * @param j the index of the entry to swap with
     */
    default void organizableplayscreens_swapEntries(int i, int j) {}
}
