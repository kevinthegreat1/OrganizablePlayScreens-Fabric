package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.mixin.ServerSelectionListMixin;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.JoinMultiplayerScreenAccessor;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.ServerSelectionListAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.server.network.EventLoopGroupHolder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiplayerFolderEntry extends AbstractMultiplayerEntry implements AbstractFolderEntry<ServerSelectionList, ServerSelectionList.Entry> {
    /**
     * All entries in this folder.
     */
    @NotNull
    private final List<ServerSelectionList.Entry> entries;
    /**
     * This button moves the selected entry into this folder.
     */
    private final Button buttonMoveInto;

    public MultiplayerFolderEntry(@NotNull JoinMultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent) {
        this(screen, parent, I18n.get("organizableplayscreens:entry.new", EntryType.FOLDER.text().getString()), new ArrayList<>());
    }

    public MultiplayerFolderEntry(@NotNull JoinMultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent, @NotNull String name) {
        this(screen, parent, name, new ArrayList<>());
    }

    public MultiplayerFolderEntry(@NotNull JoinMultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent, @NotNull String name, @NotNull List<ServerSelectionList.Entry> entries) {
        super(screen, parent, EntryType.FOLDER, name);
        this.entries = entries;
        buttonMoveInto = Button.builder(Component.nullToEmpty("+"), button -> {
            ServerSelectionList serverListWidget = ((JoinMultiplayerScreenAccessor) screen).getServerSelectionList();
            ServerSelectionList.Entry entry = serverListWidget.getSelected();
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

    public @NotNull List<ServerSelectionList.Entry> getEntries() {
        return entries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Identifier> getIcons() {
        return entries.stream()
                .filter(ServerSelectionList.OnlineServerEntry.class::isInstance)
                .map(ServerSelectionListMixin.ServerEntryAccessor.class::cast)
                .map(ServerSelectionListMixin.ServerEntryAccessor::getIcon)
                .map(FaviconTexture::textureLocation)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Button getButtonMoveInto() {
        return buttonMoveInto;
    }

    @Override
    public void entrySelectionConfirmed(ServerSelectionList serverListWidget) {
        super.entrySelectionConfirmed(serverListWidget);
        serverListWidget.organizableplayscreens_setCurrentFolder(this);
    }

    @Override
    public void render(GuiGraphicsExtractor context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize) {
        AbstractFolderEntry.super.render(context, index, y, x, mouseX, mouseY, hovered, tickDelta, name, listSize);

        // Ping servers inside folders similar to MultiplayerServerListWidget.ServerEntry#render
        for (ServerSelectionList.Entry entry : entries) {
            if (!(entry instanceof ServerSelectionList.OnlineServerEntry serverEntry)) continue;
            ServerData server = serverEntry.getServerData();

            if (server.state() == ServerData.State.INITIAL) {
                server.setState(ServerData.State.PINGING);
                server.motd = CommonComponents.EMPTY;
                server.status = CommonComponents.EMPTY;
                ServerSelectionListAccessor.getTHREAD_POOL().submit(() -> {
                    try {
                        screen.getPinger().pingServer(server, () -> client.execute(serverEntry::updateServerList), () -> {
                            server.setState(server.protocol == SharedConstants.getCurrentVersion().protocolVersion() ? ServerData.State.SUCCESSFUL : ServerData.State.INCOMPATIBLE);
                            client.execute(((ServerSelectionListMixin.ServerEntryAccessor) serverEntry)::invokeRefreshStatus);
                        }, EventLoopGroupHolder.remote(client.options.useNativeTransport()));
                    } catch (UnknownHostException e) {
                        server.setState(ServerData.State.UNREACHABLE);
                        server.motd = ServerSelectionListAccessor.getCANT_RESOLVE_TEXT();
                        client.execute(((ServerSelectionListMixin.ServerEntryAccessor) serverEntry)::invokeRefreshStatus);
                    } catch (Exception e) {
                        server.setState(ServerData.State.UNREACHABLE);
                        server.motd = ServerSelectionListAccessor.getCANT_CONNECT_TEXT();
                        client.execute(((ServerSelectionListMixin.ServerEntryAccessor) serverEntry)::invokeRefreshStatus);
                    }
                });
            }

            byte[] bs = server.getIconBytes();
            if (!Arrays.equals(bs, ((ServerSelectionListMixin.ServerEntryAccessor) serverEntry).getLastIconBytes())) {
                if (((ServerSelectionListMixin.ServerEntryAccessor) serverEntry).invokeUploadServerIcon(bs)) {
                    ((ServerSelectionListMixin.ServerEntryAccessor) serverEntry).setLastIconBytes(bs);
                } else {
                    server.setIconBytes(null);
                    serverEntry.updateServerList();
                }
            }
        }
    }

    /**
     * Handles mouse clicks for this folder.
     * <p>
     * Calls mouse click on {@link #buttonMoveInto} and {@link AbstractMultiplayerEntry#mouseClicked(MouseButtonEvent, boolean)}.
     */
    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (buttonMoveInto.mouseClicked(click, doubled)) {
            return true;
        }
        return super.mouseClicked(click, doubled);
    }
}
