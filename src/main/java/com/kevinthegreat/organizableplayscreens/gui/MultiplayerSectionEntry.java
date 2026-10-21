package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiplayerSectionEntry extends AbstractMultiplayerEntry {
    public MultiplayerSectionEntry(@NotNull JoinMultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent) {
        super(screen, parent, EntryType.SECTION);
    }

    public MultiplayerSectionEntry(@NotNull JoinMultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent, @NotNull String name, @Nullable NativeImage customIconImage) {
        super(screen, parent, EntryType.SECTION, name, customIconImage);
    }

    @Override
    public void render(GuiGraphicsExtractor context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize) {
        renderSectionEntry(context, index, y, x, mouseX, mouseY, hovered, name, listSize);
    }
}
