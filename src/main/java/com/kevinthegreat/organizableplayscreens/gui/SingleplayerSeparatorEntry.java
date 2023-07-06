package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingleplayerSeparatorEntry extends AbstractSingleplayerEntry {
    @SuppressWarnings("unused") // Used via reflection
    public SingleplayerSeparatorEntry(@NotNull SelectWorldScreen screen, @Nullable SingleplayerFolderEntry parent) {
        super(screen, parent, EntryType.SEPARATOR);
    }

    @SuppressWarnings("unused") // Used via reflection
    public SingleplayerSeparatorEntry(@NotNull SelectWorldScreen screen, @Nullable SingleplayerFolderEntry parent, @NotNull String name) {
        super(screen, parent, name);
    }

    @Override
    protected void render(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize) {
        OrganizablePlayScreens.renderSeparatorEntry(context, index, y, x, mouseX, mouseY, hovered, name, listSize);
    }
}
