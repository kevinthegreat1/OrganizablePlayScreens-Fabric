package com.kevinthegreat.organizableplayscreens.gui;

import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.TextWidget;

import java.util.List;
import java.util.SortedMap;

@SuppressWarnings("JavadocReference")
public interface WorldListWidgetAccessor {
    /**
     * Saves the folders and worlds to {@code organizable_worlds.dat}.
     */
    default void organizableplayscreens_saveFile() {}

    /**
     * Updates the displayed entries and saves the folders and worlds to {@code organizable_worlds.dat}.
     */
    default void organizableplayscreens_updateAndSave() {}

    default SingleplayerFolderEntry organizableplayscreens_getCurrentFolder() {return null;}

    default List<AbstractSingleplayerEntry> organizableplayscreens_getCurrentNonWorldEntries() {return null;}

    default List<WorldListWidget.WorldEntry> organizableplayscreens_getCurrentWorldEntries() {return null;}

    default SortedMap<WorldListWidget.WorldEntry, SingleplayerFolderEntry> organizableplayscreens_getWorlds() {return null;}

    default boolean organizableplayscreens_isRootFolder() {return false;}

    default void organizableplayscreens_setPathWidget(TextWidget pathWidget) {}

    /**
     * Sets {@link com.kevinthegreat.organizableplayscreens.mixin.WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder} and updates the displayed entries.
     *
     * @param folderEntry the new folder.
     */
    default void organizableplayscreens_setCurrentFolder(SingleplayerFolderEntry folderEntry) {}

    /**
     * Trys to set {@link com.kevinthegreat.organizableplayscreens.mixin.WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent and update the displayed entries.
     *
     * @return {@code true} if the folder has a parent and {@link com.kevinthegreat.organizableplayscreens.mixin.WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder} was changed.
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
