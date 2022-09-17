package com.kevinthegreat.organizableplayscreens;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;

import java.util.List;

public interface MultiplayerServerListWidgetAccessor {
    void organizableplayscreens_loadFile();

    void organizableplayscreens_saveFile();

    void organizableplayscreens_updateAndSave();

    FolderEntry organizableplayscreens_getCurrentFolder();

    List<MultiplayerServerListWidget.Entry> organizableplayscreens_getCurrentEntries();

    boolean organizableplayscreens_isRootFolder();

    void organizableplayscreens_setCurrentFolder(FolderEntry folderEntry);

    void organizableplayscreens_setCurrentFolderToParent();
}