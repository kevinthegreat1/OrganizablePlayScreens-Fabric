package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.gui.AbstractSingleplayerEntry;
import com.kevinthegreat.organizableplayscreens.gui.SingleplayerFolderEntry;
import com.kevinthegreat.organizableplayscreens.gui.WorldListWidgetAccessor;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.SaveVersionInfoInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Mixin(WorldListWidget.class)
public abstract class WorldListWidgetMixin extends AlwaysSelectedEntryListWidget<WorldListWidget.Entry> implements WorldListWidgetAccessor {
    @Shadow
    @Final
    private SelectWorldScreen parent;
    @Shadow
    private CompletableFuture<List<LevelSummary>> levelsFuture;
    @Shadow
    private String search;

    @Shadow
    protected abstract boolean shouldShow(String search, LevelSummary summary);

    @Shadow
    protected abstract void narrateScreenIfNarrationEnabled();

    @Shadow
    public abstract void setSelected(@Nullable WorldListWidget.Entry entry);

    /**
     * A completable future showing whether folders and worlds have been loaded.
     */
    @Unique
    private CompletableFuture<Void> organizableplayscreens_loadedFuture;
    /**
     * The root folder. Should contain all entries.
     */
    @Unique
    private final SingleplayerFolderEntry organizableplayscreens_rootFolder = new SingleplayerFolderEntry(parent, null, "root");
    /**
     * The current folder. Only entries in this folder will be displayed.
     */
    @Unique
    private SingleplayerFolderEntry organizableplayscreens_currentFolder = organizableplayscreens_rootFolder;
    /**
     * A sorted map of {@link net.minecraft.client.gui.screen.world.WorldListWidget.WorldEntry} to {@link SingleplayerFolderEntry} used for searching worlds and quickly determining which folder a world is in.
     */
    @Unique
    private final SortedMap<WorldListWidget.WorldEntry, SingleplayerFolderEntry> organizableplayscreens_worlds = new TreeMap<>(Comparator.comparing(worldEntry -> ((WorldEntryAccessor) worldEntry).getLevel()));
    /**
     * The path of {@link #organizableplayscreens_currentFolder currentFolder}.
     * <p>
     * Only used for display. In the form of '{@code folder > child folder}'. Empty in the root folder.
     */
    @Unique
    private String organizableplayscreens_currentPath;

    public WorldListWidgetMixin(MinecraftClient minecraftClient, int i, int j, int k, int l) {
        super(minecraftClient, i, j, k, l);
    }

    @Override
    public SingleplayerFolderEntry organizableplayscreens_getCurrentFolder() {
        return organizableplayscreens_currentFolder;
    }

    @Override
    public List<AbstractSingleplayerEntry> organizableplayscreens_getCurrentNonWorldEntries() {
        return organizableplayscreens_currentFolder.getNonWorldEntries();
    }

    @Override
    public List<WorldListWidget.WorldEntry> organizableplayscreens_getCurrentWorldEntries() {
        return organizableplayscreens_currentFolder.getWorldEntries();
    }

    @Override
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void organizableplayscreens_setCurrentFolder(@NotNull SingleplayerFolderEntry folderEntry) {
        organizableplayscreens_currentFolder = folderEntry;
        setSelected(null);
        organizableplayscreens_updateEntries();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Loads and displays folders and worlds from {@code oldWidget} asynchronously after it finishes loading folders and worlds and sets {@link #organizableplayscreens_loadedFuture loadedFuture}.
     */
    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/world/WorldListWidget;levelsFuture:Ljava/util/concurrent/CompletableFuture;", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER))
    private void organizableplayscreens_loadFromListWidget(SelectWorldScreen parent, MinecraftClient client, int width, int height, int y, int itemHeight, String search, WorldListWidget oldWidget, CallbackInfo ci) {
        organizableplayscreens_loadedFuture = ((WorldListWidgetMixin) (Object) oldWidget).organizableplayscreens_loadedFuture.thenRunAsync(() -> {
            organizableplayscreens_worlds.clear();
            organizableplayscreens_fromFolder(organizableplayscreens_rootFolder, ((WorldListWidgetMixin) (Object) oldWidget).organizableplayscreens_rootFolder, ((WorldListWidgetMixin) (Object) oldWidget).organizableplayscreens_currentFolder);
            organizableplayscreens_currentPath = ((WorldListWidgetMixin) (Object) oldWidget).organizableplayscreens_currentPath;
            organizableplayscreens_updateEntries();
            setSelected(null);
        }, client);
    }

    /**
     * Loads and displays folders and worlds from {@code organizable_worlds.dat} and the vanilla level list asynchronously during init and sets {@link #organizableplayscreens_loadedFuture loadedFuture}. The vanilla level list stores the level summaries while {@code organizable_worlds.dat} stores the folder structure.
     */
    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/world/WorldListWidget;levelsFuture:Ljava/util/concurrent/CompletableFuture;", opcode = Opcodes.PUTFIELD, ordinal = 1, shift = At.Shift.AFTER))
    public void organizableplayscreens_loadFileOnInit(CallbackInfo ci) {
        organizableplayscreens_loadFile(ci);
    }

    /**
     * Loads and displays folders and worlds from {@code organizable_worlds.dat} and the vanilla level list asynchronously and sets {@link #organizableplayscreens_loadedFuture loadedFuture}. The vanilla level list stores the level summaries while {@code organizable_worlds.dat} stores the folder structure.
     */
    @Inject(method = "load", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/world/WorldListWidget;levelsFuture:Ljava/util/concurrent/CompletableFuture;", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER))
    public void organizableplayscreens_loadFile(CallbackInfo ci) {
        organizableplayscreens_rootFolder.getNonWorldEntries().clear();
        organizableplayscreens_rootFolder.getWorldEntries().clear();
        organizableplayscreens_loadedFuture = levelsFuture.thenAcceptAsync(levels -> {
            try {
                organizableplayscreens_fromNbtAndUpdate(NbtIo.read(client.runDirectory.toPath().resolve("organizable_worlds.dat")), levels);
            } catch (Exception e) {
                OrganizablePlayScreens.LOGGER.error("Couldn't load world and folder list", e);
            }
        }, client);
    }

    /**
     * Loads and displays folders and worlds from {@code organizable_worlds.dat} and the vanilla level list.
     *
     * @param nbtCompound the NBT compound to read from
     * @param levels      the level list containing all the {@link LevelSummary}
     */
    @Unique
    private void organizableplayscreens_fromNbtAndUpdate(NbtCompound nbtCompound, List<LevelSummary> levels) {
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
        organizableplayscreens_updateEntries();
        setSelected(null);
    }

    /**
     * Reads the folders and worlds from {@code nbtCompound} and the vanilla level list and adds them to {@code folder}.
     *
     * @param folder      the folder to add the entries to
     * @param nbtCompound the NBT compound to read from
     * @param levels      the level list containing all the {@link LevelSummary}
     */
    @Unique
    private void organizableplayscreens_fromNbt(SingleplayerFolderEntry folder, NbtCompound nbtCompound, List<LevelSummary> levels) {
        NbtList nbtList = nbtCompound.getListOrEmpty("entries");
        folder.getWorldEntries().clear();
        folder.getNonWorldEntries().clear();
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtEntry = nbtList.getCompoundOrEmpty(i);
            OrganizablePlayScreens.updateEntryNbt(nbtEntry, false);
            String type = nbtEntry.getString("type", "");
            switch (type) {
                case "minecraft:world" -> {
                    int index = Collections.binarySearch(levels, new LevelSummary(null, SaveVersionInfoInvoker.create(0, nbtEntry.getLong("lastPlayed", 0), null, 0, null, false), nbtEntry.getString("name", ""), false, false, false, null));
                    if (index >= 0) {
                        WorldListWidget.WorldEntry worldEntry = ((WorldListWidget) (Object) this).new WorldEntry((WorldListWidget) (Object) this, levels.get(index));
                        organizableplayscreens_worlds.put(worldEntry, folder);
                        folder.getWorldEntries().add(worldEntry);
                        levels.remove(index);
                    }
                }
                case OrganizablePlayScreens.MOD_ID + ":folder" -> {
                    SingleplayerFolderEntry folderEntry = new SingleplayerFolderEntry(parent, folder, nbtEntry.getString("name", ""));
                    if (nbtEntry.getBoolean("current", false)) {
                        organizableplayscreens_currentFolder = folderEntry;
                    }
                    organizableplayscreens_fromNbt(folderEntry, nbtEntry, levels);
                    folder.getNonWorldEntries().add(folderEntry);
                }
                default -> folder.getNonWorldEntries().add(EntryType.get(Identifier.of(type)).singleplayerEntry(parent, folder, nbtEntry.getString("name", "")));
            }
        }
    }

    /**
     * Reads the folders and worlds from {@code oldFolder} and adds them to {@code newFolder}.
     *
     * @param newFolder        the new folder to add the entries to
     * @param oldFolder        the old folder to read from
     * @param oldCurrentFolder the old current folder to set the new current folder
     */
    @Unique
    private void organizableplayscreens_fromFolder(SingleplayerFolderEntry newFolder, SingleplayerFolderEntry oldFolder, SingleplayerFolderEntry oldCurrentFolder) {
        newFolder.getWorldEntries().clear();
        newFolder.getNonWorldEntries().clear();
        for (WorldListWidget.WorldEntry oldWorldEntry : oldFolder.getWorldEntries()) {
            WorldListWidget.WorldEntry newWorldEntry = ((WorldListWidget) (Object) this).new WorldEntry((WorldListWidget) (Object) this, ((WorldEntryAccessor) oldWorldEntry).getLevel());
            organizableplayscreens_worlds.put(newWorldEntry, newFolder);
            newFolder.getWorldEntries().add(newWorldEntry);
        }
        for (AbstractSingleplayerEntry oldNonWorldEntry : oldFolder.getNonWorldEntries()) {
            if (oldNonWorldEntry instanceof SingleplayerFolderEntry oldFolderEntry) {
                SingleplayerFolderEntry newFolderEntry = new SingleplayerFolderEntry(parent, newFolder, oldFolderEntry.getName());
                organizableplayscreens_fromFolder(newFolderEntry, oldFolderEntry, oldCurrentFolder);
                newFolder.getNonWorldEntries().add(newFolderEntry);
            } else {
                newFolder.getNonWorldEntries().add(oldNonWorldEntry.getType().singleplayerEntry(parent, newFolder, oldNonWorldEntry.getName()));
            }
        }
        if (oldCurrentFolder == oldFolder) {
            organizableplayscreens_currentFolder = newFolder;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void organizableplayscreens_updateAndSave() {
        organizableplayscreens_updateEntries();
        organizableplayscreens_saveFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void organizableplayscreens_saveFile() {
        try {
            NbtCompound nbtCompound = organizableplayscreens_toNbt(organizableplayscreens_rootFolder);
            Path runDirectory = client.runDirectory.toPath();
            Path tempFile = Files.createTempFile(runDirectory, "organizable_worlds", ".dat");
            NbtIo.write(nbtCompound, tempFile);
            Path backup = runDirectory.resolve("organizable_worlds.dat_old");
            Path file = runDirectory.resolve("organizable_worlds.dat");
            Util.backupAndReplace(file, tempFile, backup);
        } catch (Exception e) {
            OrganizablePlayScreens.LOGGER.error("Couldn't save world and folder list", e);
        }
    }

    /**
     * Writes the entries in {@code folder} to a NBT compound.
     *
     * @param folder the folder to read from
     * @return the NBT compound with the entries
     */
    @Unique
    private NbtCompound organizableplayscreens_toNbt(SingleplayerFolderEntry folder) {
        NbtList nbtList = new NbtList();
        for (AbstractSingleplayerEntry nonWorldEntry : folder.getNonWorldEntries()) {
            NbtCompound nbtEntry = new NbtCompound();
            if (nonWorldEntry instanceof SingleplayerFolderEntry folderEntry) {
                nbtEntry = organizableplayscreens_toNbt(folderEntry);
                if (nonWorldEntry == organizableplayscreens_currentFolder) {
                    nbtEntry.putBoolean("current", true);
                }
            }
            nbtEntry.putString("type", nonWorldEntry.getType().id().toString());
            nbtEntry.putString("name", nonWorldEntry.getName());
            nbtList.add(nbtEntry);
        }
        for (WorldListWidget.WorldEntry worldEntry : folder.getWorldEntries()) {
            NbtCompound nbtEntry = new NbtCompound();
            nbtEntry.putString("type", "minecraft:world");
            LevelSummary level = ((WorldEntryAccessor) worldEntry).getLevel();
            nbtEntry.putString("name", level.getName());
            nbtEntry.putLong("lastPlayed", level.getLastPlayed());
            nbtList.add(nbtEntry);
        }
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.put("entries", nbtList);
        return nbtCompound;
    }

    /**
     * Handles key presses for the selected entry.
     *
     * @param keyCode the key code that was pressed
     */
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void organizableplayscreens_keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        WorldListWidget.Entry entry = getSelectedOrNull();
        if (entry != null && entry.keyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Updates the displayed entries with specified search string.
     */
    @Inject(method = "showSummaries", at = @At("HEAD"), cancellable = true)
    private void organizableplayscreens_show(String search, List<LevelSummary> summaries, CallbackInfo ci) {
        organizableplayscreens_updateEntries(search);
        ci.cancel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void organizableplayscreens_swapEntries(int i, int j) {
        Collections.swap(organizableplayscreens_currentFolder.getNonWorldEntries(), i, j);
        organizableplayscreens_updateAndSave();
        setSelected(children().get(j));
        ensureVisible(getSelectedOrNull());
    }

    /**
     * Updates the displayed entries with the stored search string.
     */
    @Unique
    private void organizableplayscreens_updateEntries() {
        organizableplayscreens_updateEntries(search);
    }

    /**
     * Clears the displayed entries, sets {@link #organizableplayscreens_currentFolder currentFolder} to the folder that the selected entry is in if {@code search} is empty and the selected entry is a world entry, and displays all entries in {@link #organizableplayscreens_currentFolder currentFolder} or all entries in {@link #organizableplayscreens_worlds worlds} with the search filter applied if it is not empty.
     */
    @Unique
    private void organizableplayscreens_updateEntries(String search) {
        super.clearEntries();
        if (search.isEmpty()) {
            if (getSelectedOrNull() instanceof WorldListWidget.WorldEntry worldEntry) {
                SingleplayerFolderEntry folderEntry = organizableplayscreens_worlds.get(worldEntry);
                if (folderEntry != null) {
                    organizableplayscreens_currentFolder = folderEntry;
                    parent.worldSelected(null);
                }
            }
            children().addAll(organizableplayscreens_currentFolder.getNonWorldEntries());
            children().addAll(organizableplayscreens_currentFolder.getWorldEntries());
        } else {
            search = search.toLowerCase(Locale.ROOT);
            for (WorldListWidget.WorldEntry entry : organizableplayscreens_worlds.keySet()) {
                if (shouldShow(search, ((WorldEntryAccessor) entry).getLevel())) {
                    addEntry(entry);
                }
            }
        }
        WorldListWidget.Entry selected = getSelectedOrNull();
        if (selected == null) {
            setScrollY(0);
        } else {
            ensureVisible(selected);
        }
        narrateScreenIfNarrationEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void organizableplayscreens_updateCurrentPath() {
        List<String> path = new ArrayList<>();
        SingleplayerFolderEntry folder;
        if (search.isEmpty()) {
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

    @Mixin(WorldListWidget.WorldEntry.class)
    public interface WorldEntryAccessor {
        @Accessor
        LevelSummary getLevel();
    }
}