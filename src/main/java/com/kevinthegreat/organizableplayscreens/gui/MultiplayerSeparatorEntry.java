package com.kevinthegreat.organizableplayscreens.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.resource.language.I18n;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiplayerSeparatorEntry extends AbstractMultiplayerEntry {
    public MultiplayerSeparatorEntry(@NotNull MultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent) {
        this(screen, parent, I18n.translate("organizableplayscreens:entry.newSeparator"));
    }

    public MultiplayerSeparatorEntry(@NotNull MultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent, @NotNull String name) {
        super(screen, parent, name);
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

    }
}
