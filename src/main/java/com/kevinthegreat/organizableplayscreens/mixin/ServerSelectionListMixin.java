package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.compatibility.Compatibility;
import com.kevinthegreat.organizableplayscreens.gui.AbstractMultiplayerEntry;
import com.kevinthegreat.organizableplayscreens.gui.MultiplayerFolderEntry;
import com.kevinthegreat.organizableplayscreens.gui.MultiplayerServerListWidgetAccessor;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.JoinMultiplayerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Mixin(ServerSelectionList.class)
public abstract class ServerSelectionListMixin extends ObjectSelectionList<ServerSelectionList.Entry> implements MultiplayerServerListWidgetAccessor {
    @Shadow
    @Final
    private JoinMultiplayerScreen screen;
    @Shadow
    @Final
    private List<ServerSelectionList.OnlineServerEntry> onlineServers;
    @Shadow
    @Final
    private ServerSelectionList.Entry lanHeader;
    @Shadow
    @Final
    private List<ServerSelectionList.NetworkServerEntry> networkServers;

    @Shadow
    protected abstract void refreshEntries();

    /**
     * A comparator used to sort and search for server entries by address and then name.
     */
    @Unique
    private static final Comparator<ServerSelectionList.OnlineServerEntry> serverEntryComparator = Comparator.comparing((ServerSelectionList.OnlineServerEntry entry) -> entry.getServerData().ip).thenComparing(entry -> entry.getServerData().name);

    /**
     * The root folder. Should contain all entries.
     */
    @Unique
    @NotNull
    private final MultiplayerFolderEntry organizableplayscreens_rootFolder = new MultiplayerFolderEntry(screen, null, "root");
    /**
     * The current folder. Only entries in this folder will be displayed.
     */
    @Unique
    @NotNull
    private MultiplayerFolderEntry organizableplayscreens_currentFolder = organizableplayscreens_rootFolder;
    /**
     * @see com.kevinthegreat.organizableplayscreens.mixin.JoinMultiplayerScreenMixin#organizableplayscreens_pathWidget
     */
    @SuppressWarnings("JavadocReference")
    @Unique
    private StringWidget organizableplayscreens_pathWidget;

    public ServerSelectionListMixin(Minecraft minecraftClient, int i, int j, int k, int l) {
        super(minecraftClient, i, j, k, l);
    }

    @Override
    public MultiplayerFolderEntry organizableplayscreens_getCurrentFolder() {
        return organizableplayscreens_currentFolder;
    }

    @Override
    public List<ServerSelectionList.Entry> organizableplayscreens_getCurrentEntries() {
        return organizableplayscreens_currentFolder.getEntries();
    }

    @Override
    public boolean organizableplayscreens_isRootFolder() {
        return organizableplayscreens_currentFolder == organizableplayscreens_rootFolder;
    }

    @Override
    public void organizableplayscreens_setPathWidget(StringWidget pathWidget) {
        organizableplayscreens_pathWidget = pathWidget;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void organizableplayscreens_setCurrentFolder(@NotNull MultiplayerFolderEntry folderEntry) {
        organizableplayscreens_currentFolder = folderEntry;
        setSelected(null);
        refreshEntries();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean organizableplayscreens_setCurrentFolderToParent() {
        if (organizableplayscreens_currentFolder != organizableplayscreens_rootFolder) {
            MultiplayerFolderEntry oldCurrentFolder = organizableplayscreens_currentFolder;
            organizableplayscreens_setCurrentFolder(organizableplayscreens_currentFolder.getParent());
            setSelected(oldCurrentFolder);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void organizableplayscreens_loadFile() {
        try {
            CompoundTag nbtCompound = NbtIo.read(minecraft.gameDirectory.toPath().resolve("organizable_servers.dat"));
            List<ServerSelectionList.OnlineServerEntry> serversSorted = new ArrayList<>(onlineServers);
            serversSorted.sort(serverEntryComparator);
            if (nbtCompound != null) {
                organizableplayscreens_fromNbt(organizableplayscreens_rootFolder, nbtCompound, serversSorted);
            }
            for (ServerSelectionList.OnlineServerEntry serverEntry : onlineServers) {
                if (serversSorted.contains(serverEntry)) {
                    organizableplayscreens_currentFolder.getEntries().add(serverEntry);
                }
            }
            refreshEntries();
        } catch (Exception e) {
            OrganizablePlayScreens.LOGGER.error("Couldn't load server and folder list", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void organizableplayscreens_saveFile() {
        try {
            CompoundTag nbtCompound = organizableplayscreens_toNbt(organizableplayscreens_rootFolder);
            Path runDirectory = minecraft.gameDirectory.toPath();
            Path tempFile = Files.createTempFile(runDirectory, "organizable_servers", ".dat");
            NbtIo.write(nbtCompound, tempFile);
            Path backup = runDirectory.resolve("organizable_servers.dat_old");
            Path file = runDirectory.resolve("organizable_servers.dat");
            Util.safeReplaceFile(file, tempFile, backup);
        } catch (Exception e) {
            OrganizablePlayScreens.LOGGER.error("Couldn't save server and folder list", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void organizableplayscreens_updateAndSave() {
        refreshEntries();
        organizableplayscreens_saveFile();
    }

    /**
     * Reads the folders and servers from {@code nbtCompound} and the vanilla server list and adds them to {@code folder}.
     *
     * @param folder      the folder to add the entries to
     * @param nbtCompound the NBT compound to read from
     */
    @Unique
    private void organizableplayscreens_fromNbt(MultiplayerFolderEntry folder, CompoundTag nbtCompound, List<ServerSelectionList.OnlineServerEntry> serversSorted) {
        ListTag nbtList = nbtCompound.getListOrEmpty("entries");
        folder.getEntries().clear();
        for (int i = 0; i < nbtList.size(); i++) {
            CompoundTag nbtEntry = nbtList.getCompoundOrEmpty(i);
            OrganizablePlayScreens.updateEntryNbt(nbtEntry, true);
            String type = nbtEntry.getStringOr("type", "");
            switch (type) {
                case "minecraft:server" -> {
                    if (!nbtEntry.getBooleanOr("hidden", false)) {
                        int index = Collections.binarySearch(serversSorted, ServerEntryAccessor.create((ServerSelectionList) (Object) this, screen, new ServerData(nbtEntry.getStringOr("name", ""), nbtEntry.getStringOr("ip", ""), null)), serverEntryComparator);
                        if (index >= 0) {
                            folder.getEntries().add(serversSorted.remove(index));
                        }
                    }
                }
                case OrganizablePlayScreens.MOD_ID + ":folder" -> {
                    MultiplayerFolderEntry folderEntry = new MultiplayerFolderEntry(screen, folder, nbtEntry.getStringOr("name", ""));
                    if (nbtEntry.getBooleanOr("current", false)) {
                        organizableplayscreens_currentFolder = folderEntry;
                    }
                    organizableplayscreens_fromNbt(folderEntry, nbtEntry, serversSorted);
                    folder.getEntries().add(folderEntry);
                }
                default -> folder.getEntries().add(EntryType.get(Identifier.parse(type)).multiplayerEntry(screen, folder, nbtEntry.getStringOr("name", "")));
            }
        }
    }

    /**
     * Writes the entries in {@code folder} to a NBT compound.
     *
     * @param folder the folder to read from
     * @return the NBT compound with the entries
     */
    @Unique
    private CompoundTag organizableplayscreens_toNbt(MultiplayerFolderEntry folder) {
        ListTag nbtList = new ListTag();
        for (ServerSelectionList.Entry entry : folder.getEntries()) {
            if (entry instanceof ServerSelectionList.OnlineServerEntry serverEntry) {
                CompoundTag nbtEntry = new CompoundTag();
                nbtEntry.putString("type", "minecraft:server");
                nbtEntry.putString("ip", serverEntry.getServerData().ip);
                nbtEntry.putString("name", serverEntry.getServerData().name);
                nbtEntry.putBoolean("hidden", false);
                nbtList.add(nbtEntry);
            } else if (entry instanceof AbstractMultiplayerEntry nonServerEntry) {
                CompoundTag nbtEntry = new CompoundTag();
                if (nonServerEntry instanceof MultiplayerFolderEntry folderEntry) {
                    nbtEntry = organizableplayscreens_toNbt(folderEntry);
                    if (folderEntry == organizableplayscreens_currentFolder) {
                        nbtEntry.putBoolean("current", true);
                    }
                }
                nbtEntry.putString("type", nonServerEntry.getType().id().toString());
                nbtEntry.putString("name", nonServerEntry.getName());
                nbtList.add(nbtEntry);
            }
        }
        CompoundTag nbtCompound = new CompoundTag();
        nbtCompound.put("entries", nbtList);
        return nbtCompound;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void organizableplayscreens_swapEntries(int i, int j) {
        Collections.swap(organizableplayscreens_currentFolder.getEntries(), i, j);
        organizableplayscreens_updateAndSave();
        setSelected(children().get(j));
        scrollToEntry(getSelected());
    }

    /**
     * Clears the displayed entries and displays all entries in {@link #organizableplayscreens_currentFolder}, {@link #lanHeader}, and {@link #networkServers}.
     */
    @Inject(method = "refreshEntries", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_updateEntries(CallbackInfo ci) {
        if (Compatibility.essential_preventMultiplayerFeatures()) {
            return;
        }
        clearEntries();
        organizableplayscreens_currentFolder.getEntries().forEach(this::addEntry);
        addEntry(lanHeader);
        networkServers.forEach(this::addEntry);
        if (getSelected() == null) {
            setScrollAmount(0);
        }
        organizableplayscreens_updateCurrentPath();
        ci.cancel();
    }

    /**
     * Updates the path of {@link #organizableplayscreens_currentFolder currentFolder}.
     */
    @Unique
    private void organizableplayscreens_updateCurrentPath() {
        List<String> path = new ArrayList<>();
        MultiplayerFolderEntry folder = organizableplayscreens_currentFolder;
        while (folder.getParent() != null) {
            path.add(folder.getName());
            folder = folder.getParent();
        }
        Collections.reverse(path);
        organizableplayscreens_pathWidget.setMessage(Component.literal(String.join(" > ", path)).withColor(0xFFA0A0A0));
        ((JoinMultiplayerScreenAccessor) screen).getLayout().arrangeElements(); // Only refresh the layout positions instead of calling MultiplayerScreen#refreshWidgetPositions to avoid activating other mixins to prevent NPEs as this can run before all buttons are initialized.
    }

    @Mixin(ServerSelectionList.OnlineServerEntry.class)
    public interface ServerEntryAccessor {
        @Accessor
        FaviconTexture getIcon();

        @Accessor
        byte[] getLastIconBytes();

        @Accessor
        void setLastIconBytes(byte[] favicon);

        @SuppressWarnings("unused")
        @Invoker("<init>")
        static ServerSelectionList.OnlineServerEntry create(ServerSelectionList serverListWidget, JoinMultiplayerScreen screen, ServerData server) {
            throw new IllegalStateException("Mixin invoker failed to apply");
        }

        @Invoker
        void invokeRefreshStatus();

        @Invoker
        boolean invokeUploadServerIcon(byte[] favicon);
    }
}
