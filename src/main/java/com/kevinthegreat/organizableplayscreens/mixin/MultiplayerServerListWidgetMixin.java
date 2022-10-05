package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.MultiplayerFolderEntry;
import com.kevinthegreat.organizableplayscreens.MultiplayerServerListWidgetAccessor;
import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(MultiplayerServerListWidget.class)
public abstract class MultiplayerServerListWidgetMixin extends AlwaysSelectedEntryListWidget<MultiplayerServerListWidget.Entry> implements MultiplayerServerListWidgetAccessor {
    @Shadow
    @Final
    private MultiplayerScreen screen;
    @Shadow
    @Final
    private List<MultiplayerServerListWidget.ServerEntry> servers;
    @Shadow
    @Final
    private MultiplayerServerListWidget.Entry scanningEntry;
    @Shadow
    @Final
    private List<MultiplayerServerListWidget.LanServerEntry> lanServers;

    @Shadow
    protected abstract void updateEntries();

    /**
     * The root folder. Should contain all entries.
     */
    @NotNull
    private final MultiplayerFolderEntry organizableplayscreens_rootFolder = new MultiplayerFolderEntry(screen, null, "root");
    /**
     * The current folder. Only entries in this folder will be displayed.
     */
    @NotNull
    private MultiplayerFolderEntry organizableplayscreens_currentFolder = organizableplayscreens_rootFolder;
    /**
     * The path of {@link #organizableplayscreens_currentFolder currentFolder}.
     * <p>
     * Only used for display. In the form of '{@code folder > child folder}'. Empty in the root folder.
     */
    private String organizableplayscreens_currentPath;

    public MultiplayerServerListWidgetMixin(MinecraftClient minecraftClient, int i, int j, int k, int l, int m) {
        super(minecraftClient, i, j, k, l, m);
    }

    @Override
    public MultiplayerFolderEntry organizableplayscreens_getCurrentFolder() {
        return organizableplayscreens_currentFolder;
    }

    @Override
    public List<MultiplayerServerListWidget.Entry> organizableplayscreens_getCurrentEntries() {
        return organizableplayscreens_currentFolder.getEntries();
    }

    @Override
    public boolean organizableplayscreens_isRootFolder() {
        return organizableplayscreens_currentFolder == organizableplayscreens_rootFolder;
    }

    @Override
    public String organizableplayscreens_getCurrentPath() {
        return organizableplayscreens_currentPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void organizableplayscreens_setCurrentFolder(@NotNull MultiplayerFolderEntry folderEntry) {
        organizableplayscreens_currentFolder = folderEntry;
        screen.select(null);
        updateEntries();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean organizableplayscreens_setCurrentFolderToParent() {
        if (organizableplayscreens_currentFolder != organizableplayscreens_rootFolder) {
            MultiplayerFolderEntry oldCurrentFolder = organizableplayscreens_currentFolder;
            organizableplayscreens_setCurrentFolder(organizableplayscreens_currentFolder.getParent());
            screen.select(oldCurrentFolder);
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
            NbtCompound nbtCompound = NbtIo.read(new File(client.runDirectory, "organizable_servers.dat"));
            if (nbtCompound != null) {
                organizableplayscreens_fromNbt(organizableplayscreens_rootFolder, nbtCompound);
            } else {
                organizableplayscreens_rootFolder.getEntries().clear();
                organizableplayscreens_rootFolder.getEntries().addAll(servers);
            }
            updateEntries();
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
            NbtCompound nbtCompound = organizableplayscreens_toNbt(organizableplayscreens_rootFolder);
            File file = File.createTempFile("organizable_servers", ".dat", client.runDirectory);
            NbtIo.write(nbtCompound, file);
            File file2 = new File(client.runDirectory, "organizable_servers.dat_old");
            File file3 = new File(client.runDirectory, "organizable_servers.dat");
            Util.backupAndReplace(file3, file, file2);
        } catch (Exception e) {
            OrganizablePlayScreens.LOGGER.error("Couldn't save server and folder list", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void organizableplayscreens_updateAndSave() {
        updateEntries();
        organizableplayscreens_saveFile();
    }

    /**
     * Reads the folders and servers from {@code nbtCompound} and adds them to {@code folder}.
     *
     * @param folder      the folder to add the entries to
     * @param nbtCompound the NBT compound to read from
     */
    private void organizableplayscreens_fromNbt(MultiplayerFolderEntry folder, NbtCompound nbtCompound) {
        NbtList nbtList = nbtCompound.getList("entries", 10);
        folder.getEntries().clear();
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtEntry = nbtList.getCompound(i);
            if (!nbtEntry.getBoolean("type")) {
                if (!nbtEntry.getBoolean("hidden")) {
                    ServerInfo serverInfo = ServerInfo.fromNbt(nbtEntry);
                    folder.getEntries().add(((MultiplayerServerListWidget) (Object) this).new ServerEntry(screen, serverInfo));
                }
            } else {
                MultiplayerFolderEntry folderEntry = new MultiplayerFolderEntry(screen, folder, nbtEntry.getString("name"));
                if (nbtEntry.getBoolean("current")) {
                    organizableplayscreens_currentFolder = folderEntry;
                }
                organizableplayscreens_fromNbt(folderEntry, nbtEntry);
                folder.getEntries().add(folderEntry);
            }
        }
    }

    /**
     * Writes the entries in {@code folder} to a NBT compound.
     *
     * @param folder the folder to read from
     * @return the NBT compound with the entries
     */
    private NbtCompound organizableplayscreens_toNbt(MultiplayerFolderEntry folder) {
        NbtList nbtList = new NbtList();
        for (MultiplayerServerListWidget.Entry entry : folder.getEntries()) {
            if (entry instanceof MultiplayerServerListWidget.ServerEntry serverEntry) {
                NbtCompound nbtEntry = serverEntry.getServer().toNbt();
                nbtEntry.putBoolean("hidden", false);
                nbtList.add(nbtEntry);
            } else if (entry instanceof MultiplayerFolderEntry folderEntry) {
                NbtCompound nbtEntry = organizableplayscreens_toNbt(folderEntry);
                nbtEntry.putBoolean("type", true);
                if (folderEntry == organizableplayscreens_currentFolder) {
                    nbtEntry.putBoolean("current", true);
                }
                nbtEntry.putString("name", folderEntry.getName());
                nbtList.add(nbtEntry);
            }
        }
        NbtCompound nbtCompound = new NbtCompound();
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
        ensureSelectedEntryVisible();
    }

    /**
     * Clears the displayed entries and displays all entries in {@link #organizableplayscreens_currentFolder}, {@link #scanningEntry}, and {@link #lanServers}.
     */
    @Inject(method = "updateEntries", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_updateEntries(CallbackInfo ci) {
        clearEntries();
        children().addAll(organizableplayscreens_currentFolder.getEntries());
        children().add(scanningEntry);
        children().addAll(lanServers);
        if (getSelectedOrNull() == null) {
            setScrollAmount(0);
        }
        organizableplayscreens_updateCurrentPath();
        ci.cancel();
    }

    /**
     * Updates the path of {@link #organizableplayscreens_currentFolder currentFolder}.
     */
    private void organizableplayscreens_updateCurrentPath() {
        List<String> path = new ArrayList<>();
        MultiplayerFolderEntry folder = organizableplayscreens_currentFolder;
        while (folder.getParent() != null) {
            path.add(folder.getName());
            folder = folder.getParent();
        }
        Collections.reverse(path);
        organizableplayscreens_currentPath = String.join(" > ", path);
    }
}
