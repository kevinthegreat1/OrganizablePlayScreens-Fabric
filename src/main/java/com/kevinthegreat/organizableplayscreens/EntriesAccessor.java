package com.kevinthegreat.organizableplayscreens;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;

import java.util.List;

public interface EntriesAccessor {
    void organizableplayscreens_loadFile();

    void organizableplayscreens_saveFile();

    FolderEntry organizableplayscreens_getCurrentFolder();

    List<MultiplayerServerListWidget.Entry> organizableplayscreens_getCurrentEntries();

    void organizableplayscreens_setCurrentFolder(FolderEntry folderEntry);
}
