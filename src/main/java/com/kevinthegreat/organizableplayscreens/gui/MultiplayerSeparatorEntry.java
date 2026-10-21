package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiplayerSeparatorEntry extends AbstractMultiplayerEntry {
    public MultiplayerSeparatorEntry(@NotNull JoinMultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent) {
        super(screen, parent, EntryType.SEPARATOR);
    }

    public MultiplayerSeparatorEntry(@NotNull JoinMultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent, @NotNull String name, @Nullable NativeImage customIconImage) {
        super(screen, parent, EntryType.SEPARATOR, name, customIconImage);
    }

    @Override
    public void render(GuiGraphicsExtractor context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize) {
        renderSeparatorEntry(context, index, y, x, mouseX, mouseY, hovered, name, listSize);
    }
}
