package com.kevinthegreat.organizableplayscreens.gui;

import net.minecraft.client.gui.screen.world.WorldListWidget;

import java.util.List;
import java.util.SortedMap;

/**
 * Cast a {@link WorldListWidget} instance to this to use the following methods implemented in {@link com.kevinthegreat.organizableplayscreens.mixin.WorldListWidgetMixin WorldListWidgetMixin}.
 */
@SuppressWarnings("JavadocReference")
public interface WorldListWidgetAccessor {
    /**
     * Saves the folders and worlds to {@code organizable_worlds.dat}.
     */
    void organizableplayscreens_saveFile();

    /**
     * Updates the displayed entries and saves the folders and worlds to {@code organizable_worlds.dat}.
     */
    void organizableplayscreens_updateAndSave();

    SingleplayerFolderEntry organizableplayscreens_getCurrentFolder();

    List<AbstractSingleplayerEntry> organizableplayscreens_getCurrentNonWorldEntries();

    List<WorldListWidget.WorldEntry> organizableplayscreens_getCurrentWorldEntries();

    SortedMap<WorldListWidget.WorldEntry, SingleplayerFolderEntry> organizableplayscreens_getWorlds();

    boolean organizableplayscreens_isRootFolder();

    String organizableplayscreens_getCurrentPath();

    /**
     * Sets {@link com.kevinthegreat.organizableplayscreens.mixin.WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder} and updates the displayed entries.
     *
     * @param folderEntry the new folder.
     */
    void organizableplayscreens_setCurrentFolder(SingleplayerFolderEntry folderEntry);

    /**
     * Trys to set {@link com.kevinthegreat.organizableplayscreens.mixin.WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent and update the displayed entries.
     *
     * @return {@code true} if the folder has a parent and {@link com.kevinthegreat.organizableplayscreens.mixin.WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder} was changed.
     */
    boolean organizableplayscreens_setCurrentFolderToParent();

    /**
     * Swaps the entries at {@code i} and {@code j} and updates and saves the entries.
     *
     * @param i the index of the selected entry
     * @param j the index of the entry to swap with
     */
    void organizableplayscreens_swapEntries(int i, int j);

    /**
     * Updates the path of {@link com.kevinthegreat.organizableplayscreens.mixin.WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder}.
     *
     * @see com.kevinthegreat.organizableplayscreens.mixin.WorldListWidgetMixin#organizableplayscreens_currentPath currentPath
     */
    void organizableplayscreens_updateCurrentPath();
}
