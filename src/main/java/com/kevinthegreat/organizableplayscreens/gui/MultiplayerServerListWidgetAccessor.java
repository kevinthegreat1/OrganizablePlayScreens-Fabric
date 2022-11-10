package com.kevinthegreat.organizableplayscreens.gui;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;

import java.util.List;

/**
 * Cast a {@link MultiplayerServerListWidget} instance to this to use the following methods implemented in {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin MultiplayerServerListWidgetMixin}.
 */
@SuppressWarnings("JavadocReference")
public interface MultiplayerServerListWidgetAccessor {
    /**
     * Loads and displays folders and servers from {@code organizable_servers.dat} and the vanilla server list. The vanilla server list stores the server entries while {@code organizable_servers.dat} stores the folder structure.
     */
    void organizableplayscreens_loadFile();

    /**
     * Saves the folders and servers to {@code organizable_servers.dat}.
     */
    void organizableplayscreens_saveFile();

    /**
     * Updates the displayed entries and saves the folders and servers to {@code organizable_servers.dat}.
     */
    void organizableplayscreens_updateAndSave();

    MultiplayerFolderEntry organizableplayscreens_getCurrentFolder();

    List<MultiplayerServerListWidget.Entry> organizableplayscreens_getCurrentEntries();

    boolean organizableplayscreens_isRootFolder();

    String organizableplayscreens_getCurrentPath();

    /**
     * Sets {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} and updates the displayed entries.
     *
     * @param folderEntry the new folder.
     */
    void organizableplayscreens_setCurrentFolder(MultiplayerFolderEntry folderEntry);

    /**
     * Trys to set {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent and update the displayed entries.
     *
     * @return {@code true} if the folder has a parent and {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} was changed.
     */
    boolean organizableplayscreens_setCurrentFolderToParent();

    /**
     * Swaps the entries at {@code i} and {@code j} and updates and saves the entries.
     *
     * @param i the index of the selected entry
     * @param j the index of the entry to swap with
     */
    void organizableplayscreens_swapEntries(int i, int j);
}
