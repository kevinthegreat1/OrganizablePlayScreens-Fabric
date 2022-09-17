package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.FolderEntry;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
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
    public abstract void updateEntries();

    private final FolderEntry organizableplayscreens_rootFolder = new FolderEntry(screen, null, "root");
    private FolderEntry organizableplayscreens_currentFolder = organizableplayscreens_rootFolder;

    public MultiplayerServerListWidgetMixin(MinecraftClient minecraftClient, int i, int j, int k, int l, int m) {
        super(minecraftClient, i, j, k, l, m);
    }

    @Override
    public FolderEntry organizableplayscreens_getCurrentFolder() {
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
    public void organizableplayscreens_setCurrentFolder(FolderEntry folderEntry) {
        organizableplayscreens_currentFolder = folderEntry;
        screen.select(null);
        updateEntries();
    }

    @Override
    public void organizableplayscreens_setCurrentFolderToParent() {
        organizableplayscreens_setCurrentFolder(organizableplayscreens_currentFolder.getParent());
    }

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

    @Override
    public void organizableplayscreens_updateAndSave(){
        updateEntries();
        organizableplayscreens_saveFile();
    }

    private FolderEntry organizableplayscreens_fromNbt(FolderEntry folder, NbtCompound nbtCompound) {
        NbtList nbtList = nbtCompound.getList("entries", 10);
        folder.getEntries().clear();
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtEntry = nbtList.getCompound(i);
            if (!nbtEntry.getBoolean("type")) {
                if (!nbtEntry.getBoolean("hidden")) {
                    ServerInfo serverInfo = ServerInfo.fromNbt(nbtEntry);
                    folder.getEntries().add(((MultiplayerServerListWidget) ((Object) this)).new ServerEntry(screen, serverInfo));
                }
            } else {
                folder.getEntries().add(organizableplayscreens_fromNbt(new FolderEntry(screen, folder, nbtEntry.getString("name")), nbtEntry));
            }
        }
        return folder;
    }

    private NbtCompound organizableplayscreens_toNbt(FolderEntry folder) {
        NbtList nbtList = new NbtList();
        for (MultiplayerServerListWidget.Entry entry : folder.getEntries()) {
            if (entry instanceof MultiplayerServerListWidget.ServerEntry serverEntry) {
                NbtCompound nbtEntry = serverEntry.getServer().toNbt();
                nbtEntry.putBoolean("hidden", false);
                nbtList.add(nbtEntry);
            } else if (entry instanceof FolderEntry folderEntry) {
                NbtCompound nbtEntry = organizableplayscreens_toNbt(folderEntry);
                nbtEntry.putString("name", folderEntry.getName());
                nbtEntry.putBoolean("type", true);
                nbtList.add(nbtEntry);
            }
        }
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.put("entries", nbtList);
        return nbtCompound;
    }

    @Inject(method = "updateEntries", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_updateEntries(CallbackInfo ci) {
        clearEntries();
        children().addAll(organizableplayscreens_currentFolder.getEntries());
        children().add(scanningEntry);
        children().addAll(lanServers);
        ci.cancel();
    }
}
