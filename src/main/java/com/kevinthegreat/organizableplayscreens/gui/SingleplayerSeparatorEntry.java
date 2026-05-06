package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingleplayerSeparatorEntry extends AbstractSingleplayerEntry {
    public SingleplayerSeparatorEntry(@NotNull SelectWorldScreen screen, @Nullable SingleplayerFolderEntry parent) {
        super(screen, parent, EntryType.SEPARATOR);
    }

    public SingleplayerSeparatorEntry(@NotNull SelectWorldScreen screen, @Nullable SingleplayerFolderEntry parent, @NotNull String name) {
        super(screen, parent, EntryType.SEPARATOR, name);
    }

    @Override
    public void render(GuiGraphicsExtractor context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize) {
        AbstractEntry.renderSeparatorEntry(context, index, y, x, mouseX, mouseY, hovered, name, listSize);
    }
}
