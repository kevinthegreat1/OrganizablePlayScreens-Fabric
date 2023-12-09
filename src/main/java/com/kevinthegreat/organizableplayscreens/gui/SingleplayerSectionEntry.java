package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingleplayerSectionEntry extends AbstractSingleplayerEntry {
    public SingleplayerSectionEntry(@NotNull SelectWorldScreen screen, @Nullable SingleplayerFolderEntry parent) {
        super(screen, parent, EntryType.SECTION);
    }

    public SingleplayerSectionEntry(@NotNull SelectWorldScreen screen, @Nullable SingleplayerFolderEntry parent, @NotNull String name) {
        super(screen, parent, EntryType.SECTION, name);
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize) {
        AbstractEntry.renderSectionEntry(context, index, y, x, mouseX, mouseY, hovered, name, listSize);
    }
}
