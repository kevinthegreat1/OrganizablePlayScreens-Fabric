package com.kevinthegreat.organizableplayscreens;

import net.minecraft.client.gui.screen.world.WorldListWidget;

import java.util.List;
import java.util.SortedMap;

public interface WorldListWidgetAccessor {
    void organizableplayscreens_saveFile();

    void organizableplayscreens_updateAndSave();

    SingleplayerFolderEntry organizableplayscreens_getCurrentFolder();

    List<SingleplayerFolderEntry> organizableplayscreens_getCurrentFolderEntries();

    List<WorldListWidget.WorldEntry> organizableplayscreens_getCurrentWorldEntries();

    SortedMap<WorldListWidget.WorldEntry, SingleplayerFolderEntry> organizableplayscreens_getWorlds();

    boolean organizableplayscreens_isRootFolder();

    String organizableplayscreens_getCurrentPath();

    void organizableplayscreens_setCurrentFolder(SingleplayerFolderEntry folderEntry);

    boolean organizableplayscreens_setCurrentFolderToParent();

    void organizableplayscreens_swapEntries(int i, int j);

    void organizableplayscreens_updateCurrentPath();
}
