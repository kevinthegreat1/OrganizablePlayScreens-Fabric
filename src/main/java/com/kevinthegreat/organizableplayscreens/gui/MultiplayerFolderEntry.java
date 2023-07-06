package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.MultiplayerScreenAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MultiplayerFolderEntry extends AbstractMultiplayerEntry {
    /**
     * All entries in this folder.
     */
    @NotNull
    private final List<MultiplayerServerListWidget.Entry> entries;
    /**
     * This button moves the selected entry into this folder.
     */
    private final ButtonWidget buttonMoveInto;

    public MultiplayerFolderEntry(@NotNull MultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent) {
        this(screen, parent, I18n.translate("organizableplayscreens:entry.new", EntryType.FOLDER.text().getString()), new ArrayList<>());
    }

    public MultiplayerFolderEntry(@NotNull MultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent, @NotNull String name) {
        this(screen, parent, name, new ArrayList<>());
    }

    public MultiplayerFolderEntry(@NotNull MultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent, @NotNull String name, @NotNull List<MultiplayerServerListWidget.Entry> entries) {
        super(screen, parent, EntryType.FOLDER, name);
        this.entries = entries;
        buttonMoveInto = ButtonWidget.builder(Text.of("+"), button -> {
            MultiplayerServerListWidget serverListWidget = ((MultiplayerScreenAccessor) screen).getServerListWidget();
            MultiplayerServerListWidget.Entry entry = serverListWidget.getSelectedOrNull();
            if (entry != null) {
                if (entry instanceof AbstractMultiplayerEntry nonServerEntry) {
                    nonServerEntry.parent = this;
                }
                entries.add(entry);
                ((MultiplayerServerListWidgetAccessor) serverListWidget).organizableplayscreens_getCurrentEntries().remove(entry);
                ((MultiplayerServerListWidgetAccessor) serverListWidget).organizableplayscreens_updateAndSave();
            }
        }).width(20).tooltip(OrganizablePlayScreens.MOVE_ENTRY_INTO_TOOLTIP).build();
    }

    public @NotNull List<MultiplayerServerListWidget.Entry> getEntries() {
        return entries;
    }

    @Override
    protected void render(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize) {
        OrganizablePlayScreens.renderFolderEntry(context, index, y, x, mouseX, mouseY, hovered, tickDelta, name, listSize, buttonMoveInto);
    }

    /**
     * Handles mouse clicks for this folder.
     * <p>
     * Calls mouse click on {@link #buttonMoveInto} and {@link AbstractMultiplayerEntry#mouseClicked(double, double, int)}.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (buttonMoveInto.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Updates the activation state of {@link #buttonMoveInto}.
     */
    public void updateButtonStates() {
        MultiplayerServerListWidget.Entry entry = ((MultiplayerScreenAccessor) screen).getServerListWidget().getSelectedOrNull();
        buttonMoveInto.active = entry != null && entry != this;
    }
}
