package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiplayerSeparatorEntry extends AbstractMultiplayerEntry {
    public MultiplayerSeparatorEntry(@NotNull MultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent) {
        super(screen, parent, EntryType.SEPARATOR);
    }

    public MultiplayerSeparatorEntry(@NotNull MultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent, @NotNull String name) {
        super(screen, parent, EntryType.SEPARATOR, name);
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize) {
        AbstractEntry.renderSeparatorEntry(context, index, y, x, mouseX, mouseY, hovered, name, listSize);
    }
}
