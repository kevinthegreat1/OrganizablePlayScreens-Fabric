package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.SingleplayerFolderEntry;
import com.kevinthegreat.organizableplayscreens.WorldListWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.SaveVersionInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mixin(WorldListWidget.class)
public abstract class WorldListWidgetMixin extends AlwaysSelectedEntryListWidget<WorldListWidget.Entry> implements WorldListWidgetAccessor {
    @Shadow
    @Final
    private SelectWorldScreen parent;
    @Shadow
    @Nullable
    private CompletableFuture<List<LevelSummary>> levelsFuture;

    @Shadow
    protected abstract boolean shouldShow(String search, LevelSummary summary);

    @Shadow
    protected abstract void showLoadingScreen();

    @Shadow
    protected abstract void narrateScreenIfNarrationEnabled();

    @Shadow
    public abstract void setSelected(@Nullable WorldListWidget.Entry entry);

    private CompletableFuture<Void> organizableplayscreens_loadedFuture;
    private final SingleplayerFolderEntry organizableplayscreens_rootFolder = new SingleplayerFolderEntry(parent, null, "root");
    private SingleplayerFolderEntry organizableplayscreens_currentFolder = organizableplayscreens_rootFolder;
    private final SortedMap<WorldListWidget.WorldEntry, SingleplayerFolderEntry> organizableplayscreens_worlds = new TreeMap<>(Comparator.comparing(worldEntry -> worldEntry.level));
    private String organizableplayscreens_currentPath;

    public WorldListWidgetMixin(MinecraftClient minecraftClient, int i, int j, int k, int l, int m) {
        super(minecraftClient, i, j, k, l, m);
    }

    @Override
    public SingleplayerFolderEntry organizableplayscreens_getCurrentFolder() {
        return organizableplayscreens_currentFolder;
    }

    @Override
    public List<SingleplayerFolderEntry> organizableplayscreens_getCurrentFolderEntries() {
        return organizableplayscreens_currentFolder.getFolderEntries();
    }

    @Override
    public List<WorldListWidget.WorldEntry> organizableplayscreens_getCurrentWorldEntries() {
        return organizableplayscreens_currentFolder.getWorldEntries();
    }

    public SortedMap<WorldListWidget.WorldEntry, SingleplayerFolderEntry> organizableplayscreens_getWorlds() {
        return organizableplayscreens_worlds;
    }

    @Override
    public boolean organizableplayscreens_isRootFolder() {
        return organizableplayscreens_currentFolder == organizableplayscreens_rootFolder;
    }

    @Override
    public String organizableplayscreens_getCurrentPath() {
        return organizableplayscreens_currentPath;
    }

    @Override
    public void organizableplayscreens_setCurrentFolder(@NotNull SingleplayerFolderEntry folderEntry) {
        organizableplayscreens_currentFolder = folderEntry;
        setSelected(null);
        organizableplayscreens_updateEntries(parent.getSearchFilter().get());
    }

    @Override
    public boolean organizableplayscreens_setCurrentFolderToParent() {
        if (organizableplayscreens_currentFolder != organizableplayscreens_rootFolder) {
            SingleplayerFolderEntry oldCurrentFolder = organizableplayscreens_currentFolder;
            organizableplayscreens_setCurrentFolder(organizableplayscreens_currentFolder.getParent());
            setSelected(oldCurrentFolder);
            return true;
        }
        return false;
    }

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/world/WorldListWidget;levelsFuture:Ljava/util/concurrent/CompletableFuture;", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER))
    private void organizableplayscreens_loadFromListWidget(SelectWorldScreen parent, MinecraftClient client, int width, int height, int top, int bottom, int itemHeight, Supplier<String> searchFilter, WorldListWidget oldWidget, CallbackInfo ci) {
        showLoadingScreen();
        organizableplayscreens_loadedFuture = ((WorldListWidgetMixin) (Object) oldWidget).organizableplayscreens_loadedFuture.thenRunAsync(() -> {
            organizableplayscreens_worlds.clear();
            organizableplayscreens_fromFolder(organizableplayscreens_rootFolder, ((WorldListWidgetMixin) (Object) oldWidget).organizableplayscreens_rootFolder, ((WorldListWidgetMixin) (Object) oldWidget).organizableplayscreens_currentFolder);
            organizableplayscreens_currentPath = ((WorldListWidgetMixin) (Object) oldWidget).organizableplayscreens_currentPath;
            organizableplayscreens_updateEntries(searchFilter.get());
            setSelected(null);
        }, client);
    }

    @Inject(method = "loadAndShow", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/world/WorldListWidget;levelsFuture:Ljava/util/concurrent/CompletableFuture;", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    public void organizableplayscreens_loadFile(Supplier<String> searchFilter, CallbackInfo ci) {
        showLoadingScreen();
        organizableplayscreens_rootFolder.getFolderEntries().clear();
        organizableplayscreens_rootFolder.getWorldEntries().clear();
        organizableplayscreens_loadedFuture = levelsFuture.thenAcceptAsync((levels -> {
            try {
                organizableplayscreens_fromNbtAndUpdate(searchFilter, NbtIo.read(new File(client.runDirectory, "organizable_worlds.dat")), levels);
            } catch (Exception e) {
                OrganizablePlayScreens.LOGGER.error("Couldn't load world and folder list", e);
            }
        }), client);
        ci.cancel();
    }

    private void organizableplayscreens_fromNbtAndUpdate(Supplier<String> searchFilter, NbtCompound nbtCompound, List<LevelSummary> levels) {
        levels = new ArrayList<>(levels);
        if (nbtCompound != null) {
            organizableplayscreens_fromNbt(organizableplayscreens_rootFolder, nbtCompound, levels);
        }
        for (LevelSummary levelSummary : levels) {
            WorldListWidget.WorldEntry worldEntry = ((WorldListWidget) (Object) this).new WorldEntry((WorldListWidget) (Object) this, levelSummary);
            organizableplayscreens_worlds.put(worldEntry, organizableplayscreens_currentFolder);
            organizableplayscreens_currentFolder.getWorldEntries().add(worldEntry);
        }
        OrganizablePlayScreens.sortWorldEntries(organizableplayscreens_currentFolder.getWorldEntries());
        organizableplayscreens_updateEntries(searchFilter.get());
    }

    private void organizableplayscreens_fromNbt(SingleplayerFolderEntry folder, NbtCompound nbtCompound, List<LevelSummary> levels) {
        NbtList nbtList = nbtCompound.getList("entries", 10);
        folder.getWorldEntries().clear();
        folder.getFolderEntries().clear();
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtEntry = nbtList.getCompound(i);
            if (!nbtEntry.getBoolean("type")) {
                int index = Collections.binarySearch(levels, new LevelSummary(null, new SaveVersionInfo(0, nbtEntry.getLong("lastPlayed"), null, 0, null, false), nbtEntry.getString("name"), false, false, null));
                if (index >= 0) {
                    WorldListWidget.WorldEntry worldEntry = ((WorldListWidget) (Object) this).new WorldEntry((WorldListWidget) (Object) this, levels.get(index));
                    organizableplayscreens_worlds.put(worldEntry, folder);
                    folder.getWorldEntries().add(worldEntry);
                    levels.remove(index);
                }
            } else {
                SingleplayerFolderEntry folderEntry = new SingleplayerFolderEntry(parent, folder, nbtEntry.getString("name"));
                if (nbtEntry.getBoolean("current")) {
                    organizableplayscreens_currentFolder = folderEntry;
                }
                organizableplayscreens_fromNbt(folderEntry, nbtEntry, levels);
                folder.getFolderEntries().add(folderEntry);
            }
        }
    }

    private void organizableplayscreens_fromFolder(SingleplayerFolderEntry newFolder, SingleplayerFolderEntry oldFolder, SingleplayerFolderEntry oldCurrentFolder) {
        newFolder.getWorldEntries().clear();
        newFolder.getFolderEntries().clear();
        for (WorldListWidget.WorldEntry oldWorldEntry : oldFolder.getWorldEntries()) {
            WorldListWidget.WorldEntry newWorldEntry = ((WorldListWidget) (Object) this).new WorldEntry((WorldListWidget) (Object) this, oldWorldEntry.level);
            organizableplayscreens_worlds.put(newWorldEntry, newFolder);
            newFolder.getWorldEntries().add(newWorldEntry);
        }
        for (SingleplayerFolderEntry oldFolderEntry : oldFolder.getFolderEntries()) {
            SingleplayerFolderEntry newFolderEntry = new SingleplayerFolderEntry(parent, newFolder, oldFolderEntry.getName());
            organizableplayscreens_fromFolder(newFolderEntry, oldFolderEntry, oldCurrentFolder);
            newFolder.getFolderEntries().add(newFolderEntry);
        }
        if (oldCurrentFolder == oldFolder) {
            organizableplayscreens_currentFolder = newFolder;
        }
    }

    @Override
    public void organizableplayscreens_updateAndSave() {
        organizableplayscreens_updateEntries(parent.getSearchFilter().get());
        organizableplayscreens_saveFile();
    }

    @Override
    public void organizableplayscreens_saveFile() {
        try {
            NbtCompound nbtCompound = organizableplayscreens_toNbt(organizableplayscreens_rootFolder);
            File file = File.createTempFile("organizable_worlds", ".dat", client.runDirectory);
            NbtIo.write(nbtCompound, file);
            File file2 = new File(client.runDirectory, "organizable_worlds.dat_old");
            File file3 = new File(client.runDirectory, "organizable_worlds.dat");
            Util.backupAndReplace(file3, file, file2);
        } catch (Exception e) {
            OrganizablePlayScreens.LOGGER.error("Couldn't save world and folder list", e);
        }
    }

    private NbtCompound organizableplayscreens_toNbt(SingleplayerFolderEntry folder) {
        NbtList nbtList = new NbtList();
        for (SingleplayerFolderEntry folderEntry : folder.getFolderEntries()) {
            NbtCompound nbtEntry = organizableplayscreens_toNbt(folderEntry);
            nbtEntry.putBoolean("type", true);
            if (folderEntry == organizableplayscreens_currentFolder) {
                nbtEntry.putBoolean("current", true);
            }
            nbtEntry.putString("name", folderEntry.getName());
            nbtList.add(nbtEntry);
        }
        for (WorldListWidget.WorldEntry worldEntry : folder.getWorldEntries()) {
            NbtCompound nbtEntry = new NbtCompound();
            nbtEntry.putString("name", worldEntry.level.getName());
            nbtEntry.putLong("lastPlayed", worldEntry.level.getLastPlayed());
            nbtList.add(nbtEntry);
        }
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.put("entries", nbtList);
        return nbtCompound;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        WorldListWidget.Entry entry = getSelectedOrNull();
        return entry != null && entry.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void organizableplayscreens_swapEntries(int i, int j) {
        Collections.swap(organizableplayscreens_currentFolder.getFolderEntries(), i, j);
        organizableplayscreens_updateAndSave();
        setSelected(children().get(j));
        ensureSelectedEntryVisible();
    }

    @Inject(method = "filter", at = @At("HEAD"), cancellable = true)
    private void organizableplayscreens_filter(String search, CallbackInfo ci) {
        organizableplayscreens_updateEntries(search);
        ci.cancel();
    }

    private void organizableplayscreens_updateEntries(String search) {
        clearEntries();
        if (search.isEmpty()) {
            if (getSelectedOrNull() instanceof WorldListWidget.WorldEntry worldEntry) {
                SingleplayerFolderEntry folderEntry = organizableplayscreens_worlds.get(worldEntry);
                if (folderEntry != null) {
                    organizableplayscreens_currentFolder = folderEntry;
                    parent.worldSelected(true);
                }
            }
            children().addAll(organizableplayscreens_currentFolder.getFolderEntries());
            children().addAll(organizableplayscreens_currentFolder.getWorldEntries());
        } else {
            search = search.toLowerCase(Locale.ROOT);
            for (WorldListWidget.WorldEntry entry : organizableplayscreens_worlds.keySet()) {
                if (shouldShow(search, entry.level)) {
                    addEntry(entry);
                }
            }
        }
        if (getSelectedOrNull() == null) {
            setScrollAmount(0);
        }
        ensureSelectedEntryVisible();
        narrateScreenIfNarrationEnabled();
    }

    public void organizableplayscreens_updateCurrentPath() {
        List<String> path = new ArrayList<>();
        SingleplayerFolderEntry folder;
        if (parent.getSearchFilter().get().isEmpty()) {
            folder = organizableplayscreens_currentFolder;
        } else if (getSelectedOrNull() instanceof WorldListWidget.WorldEntry worldEntry) {
            folder = organizableplayscreens_worlds.get(worldEntry);
        } else {
            organizableplayscreens_currentPath = "";
            return;
        }
        while (folder.getParent() != null) {
            path.add(folder.getName());
            folder = folder.getParent();
        }
        Collections.reverse(path);
        organizableplayscreens_currentPath = String.join(" > ", path);
    }
}