package com.kevinthegreat.organizableplayscreens;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;

import java.util.List;

public interface MultiplayerServerListWidgetAccessor {
    void organizableplayscreens_loadFile();

    void organizableplayscreens_saveFile();

    void organizableplayscreens_updateAndSave();

    MultiplayerFolderEntry organizableplayscreens_getCurrentFolder();

    List<MultiplayerServerListWidget.Entry> organizableplayscreens_getCurrentEntries();

    boolean organizableplayscreens_isRootFolder();

    String organizableplayscreens_getCurrentPath();

    void organizableplayscreens_setCurrentFolder(MultiplayerFolderEntry folderEntry);

    boolean organizableplayscreens_setCurrentFolderToParent();

    void organizableplayscreens_swapEntries(int i, int j);
}
