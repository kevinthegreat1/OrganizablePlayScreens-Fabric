package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.MultiplayerScreenAccessor;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.MultiplayerServerListWidgetAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.screen.world.WorldIcon;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
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
    public List<Identifier> getIcons() {
        return entries.stream()
                .filter(MultiplayerServerListWidget.ServerEntry.class::isInstance)
                .map(MultiplayerServerListWidgetMixin.ServerEntryAccessor.class::cast)
                .map(MultiplayerServerListWidgetMixin.ServerEntryAccessor::getIcon)
                .map(WorldIcon::getTextureId)
                .toList();
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

    @Override
    public void render(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize) {
        AbstractFolderEntry.super.render(context, index, y, x, mouseX, mouseY, hovered, tickDelta, name, listSize);

        // Ping servers inside folders similar to MultiplayerServerListWidget.ServerEntry#render
        for (MultiplayerServerListWidget.Entry entry : entries) {
            if (!(entry instanceof MultiplayerServerListWidget.ServerEntry serverEntry)) continue;
            ServerInfo server = serverEntry.getServer();

            if (server.getStatus() == ServerInfo.Status.INITIAL) {
                server.setStatus(ServerInfo.Status.PINGING);
                server.label = ScreenTexts.EMPTY;
                server.playerCountLabel = ScreenTexts.EMPTY;
                MultiplayerServerListWidgetAccessor.getSERVER_PINGER_THREAD_POOL().submit(() -> {
                    try {
                        screen.getServerListPinger().add(server, () -> client.execute(serverEntry::saveFile), () -> {
                            server.setStatus(server.protocolVersion == SharedConstants.getGameVersion().protocolVersion() ? ServerInfo.Status.SUCCESSFUL : ServerInfo.Status.INCOMPATIBLE);
                            client.execute(((MultiplayerServerListWidgetMixin.ServerEntryAccessor) serverEntry)::invokeUpdate);
                        });
                    } catch (UnknownHostException e) {
                        server.setStatus(ServerInfo.Status.UNREACHABLE);
                        server.label = MultiplayerServerListWidgetAccessor.getCANNOT_RESOLVE_TEXT();
                        client.execute(((MultiplayerServerListWidgetMixin.ServerEntryAccessor) serverEntry)::invokeUpdate);
                    } catch (Exception e) {
                        server.setStatus(ServerInfo.Status.UNREACHABLE);
                        server.label = MultiplayerServerListWidgetAccessor.getCANNOT_CONNECT_TEXT();
                        client.execute(((MultiplayerServerListWidgetMixin.ServerEntryAccessor) serverEntry)::invokeUpdate);
                    }
                });
            }

            byte[] bs = server.getFavicon();
            if (!Arrays.equals(bs, ((MultiplayerServerListWidgetMixin.ServerEntryAccessor) serverEntry).getFavicon())) {
                if (((MultiplayerServerListWidgetMixin.ServerEntryAccessor) serverEntry).invokeUploadFavicon(bs)) {
                    ((MultiplayerServerListWidgetMixin.ServerEntryAccessor) serverEntry).setFavicon(bs);
                } else {
                    server.setFavicon(null);
                    serverEntry.saveFile();
                }
            }
        }
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
