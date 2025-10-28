package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.MultiplayerScreenAccessor;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MultiplayerFolderEntry extends AbstractMultiplayerEntry implements AbstractFolderEntry<MultiplayerServerListWidget, MultiplayerServerListWidget.Entry> {
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
                serverListWidget.organizableplayscreens_getCurrentEntries().remove(entry);
                serverListWidget.organizableplayscreens_updateAndSave();
            }
        }).width(20).tooltip(OrganizablePlayScreens.MOVE_ENTRY_INTO_TOOLTIP).build();
    }

    public @NotNull List<MultiplayerServerListWidget.Entry> getEntries() {
        return entries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ButtonWidget getButtonMoveInto() {
        return buttonMoveInto;
    }

    @Override
    public void entrySelectionConfirmed(MultiplayerServerListWidget serverListWidget) {
        super.entrySelectionConfirmed(serverListWidget);
        serverListWidget.organizableplayscreens_setCurrentFolder(this);
    }

    /**
     * Handles mouse clicks for this folder.
     * <p>
     * Calls mouse click on {@link #buttonMoveInto} and {@link AbstractMultiplayerEntry#mouseClicked(Click, boolean)}.
     */
    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (buttonMoveInto.mouseClicked(click, doubled)) {
            return true;
        }
        return super.mouseClicked(click, doubled);
    }
}
