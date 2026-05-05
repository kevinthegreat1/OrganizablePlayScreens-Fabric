package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.gui.AbstractSingleplayerEntry;
import com.kevinthegreat.organizableplayscreens.gui.SingleplayerFolderEntry;
import com.kevinthegreat.organizableplayscreens.gui.WorldListWidgetAccessor;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.LevelVersionInvoker;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.SelectWorldScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList.WorldListEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Mixin(WorldSelectionList.class)
public abstract class WorldSelectionListMixin extends ObjectSelectionList<WorldSelectionList.Entry> implements WorldListWidgetAccessor {
    @Shadow
    @Final
    private Screen screen;
    @Shadow
    private CompletableFuture<List<LevelSummary>> pendingLevels;
    @Shadow
    private String filter;

    @Shadow
    protected abstract boolean filterAccepts(String search, LevelSummary summary);

    @Shadow
    protected abstract void notifyListUpdated();

    @Shadow
    public abstract void setSelected(@Nullable WorldSelectionList.Entry entry);

    /**
     * A completable future showing whether folders and worlds have been loaded.
     */
    @Unique
    private CompletableFuture<Void> organizableplayscreens_loadedFuture;
    /**
     * The root folder. Should contain all entries.
     */
    @Unique
    private final SingleplayerFolderEntry organizableplayscreens_rootFolder = new SingleplayerFolderEntry((SelectWorldScreen) screen, null, "root");
    /**
     * The current folder. Only entries in this folder will be displayed.
     */
    @Unique
    private SingleplayerFolderEntry organizableplayscreens_currentFolder = organizableplayscreens_rootFolder;
    /**
     * A sorted map of {@link WorldSelectionList.WorldListEntry} to {@link SingleplayerFolderEntry} used for searching worlds and quickly determining which folder a world is in.
     */
    @Unique
    private final SortedMap<WorldSelectionList.WorldListEntry, SingleplayerFolderEntry> organizableplayscreens_worlds = new TreeMap<>(Comparator.comparing(worldEntry -> ((WorldEntryAccessor) worldEntry).getSummary()));
    /**
     * @see com.kevinthegreat.organizableplayscreens.mixin.SelectWorldScreenMixin#organizableplayscreens_pathWidget
     */
    @SuppressWarnings("JavadocReference")
    @Unique
    private StringWidget organizableplayscreens_pathWidget;

    public WorldSelectionListMixin(Minecraft minecraftClient, int i, int j, int k, int l) {
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
    public List<WorldSelectionList.WorldListEntry> organizableplayscreens_getCurrentWorldEntries() {
        return organizableplayscreens_currentFolder.getWorldEntries();
    }

    @Override
    public SortedMap<WorldSelectionList.WorldListEntry, SingleplayerFolderEntry> organizableplayscreens_getWorlds() {
        return organizableplayscreens_worlds;
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
    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldSelectionList;pendingLevels:Ljava/util/concurrent/CompletableFuture;", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER))
    private void organizableplayscreens_loadFromListWidget(Screen parent, Minecraft client, int width, int height, String search, WorldSelectionList oldWidget, Consumer<LevelSummary> selectionCallback, Consumer<WorldSelectionList.WorldListEntry> confirmationCallback, WorldSelectionList.EntryType worldListType, CallbackInfo ci) {
        organizableplayscreens_loadedFuture = ((WorldSelectionListMixin) (Object) oldWidget).organizableplayscreens_loadedFuture.thenRunAsync(() -> {
            organizableplayscreens_worlds.clear();
            organizableplayscreens_fromFolder(organizableplayscreens_rootFolder, ((WorldSelectionListMixin) (Object) oldWidget).organizableplayscreens_rootFolder, ((WorldSelectionListMixin) (Object) oldWidget).organizableplayscreens_currentFolder);
            organizableplayscreens_pathWidget = ((WorldSelectionListMixin) (Object) oldWidget).organizableplayscreens_pathWidget;
            organizableplayscreens_updateEntries();
            setSelected(null);
        }, client);
    }

    /**
     * Loads and displays folders and worlds from {@code organizable_worlds.dat} and the vanilla level list asynchronously during init and sets {@link #organizableplayscreens_loadedFuture loadedFuture}. The vanilla level list stores the level summaries while {@code organizable_worlds.dat} stores the folder structure.
     */
    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldSelectionList;pendingLevels:Ljava/util/concurrent/CompletableFuture;", opcode = Opcodes.PUTFIELD, ordinal = 1, shift = At.Shift.AFTER))
    public void organizableplayscreens_loadFileOnInit(CallbackInfo ci) {
        organizableplayscreens_loadFile(ci);
    }

    /**
     * Loads and displays folders and worlds from {@code organizable_worlds.dat} and the vanilla level list asynchronously and sets {@link #organizableplayscreens_loadedFuture loadedFuture}. The vanilla level list stores the level summaries while {@code organizable_worlds.dat} stores the folder structure.
     */
    @Inject(method = "reloadWorldList", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldSelectionList;pendingLevels:Ljava/util/concurrent/CompletableFuture;", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER))
    public void organizableplayscreens_loadFile(CallbackInfo ci) {
        organizableplayscreens_rootFolder.getNonWorldEntries().clear();
        organizableplayscreens_rootFolder.getWorldEntries().clear();
        organizableplayscreens_loadedFuture = pendingLevels.thenAcceptAsync(levels -> {
            try {
                organizableplayscreens_fromNbtAndUpdate(NbtIo.read(minecraft.gameDirectory.toPath().resolve("organizable_worlds.dat")), levels);
            } catch (Exception e) {
                OrganizablePlayScreens.LOGGER.error("Couldn't load world and folder list", e);
            }
        }, minecraft);
    }

    /**
     * Loads and displays folders and worlds from {@code organizable_worlds.dat} and the vanilla level list.
     *
     * @param nbtCompound the NBT compound to read from
     * @param levels      the level list containing all the {@link LevelSummary}
     */
    @Unique
    private void organizableplayscreens_fromNbtAndUpdate(CompoundTag nbtCompound, List<LevelSummary> levels) {
        levels = new ArrayList<>(levels);
        if (nbtCompound != null) {
            organizableplayscreens_fromNbt(organizableplayscreens_rootFolder, nbtCompound, levels);
        }
        for (LevelSummary levelSummary : levels) {
            WorldSelectionList.WorldListEntry worldEntry = ((WorldSelectionList) (Object) this).new WorldListEntry((WorldSelectionList) (Object) this, levelSummary);
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
    private void organizableplayscreens_fromNbt(SingleplayerFolderEntry folder, CompoundTag nbtCompound, List<LevelSummary> levels) {
        ListTag nbtList = nbtCompound.getListOrEmpty("entries");
        folder.getWorldEntries().clear();
        folder.getNonWorldEntries().clear();
        for (int i = 0; i < nbtList.size(); i++) {
            CompoundTag nbtEntry = nbtList.getCompoundOrEmpty(i);
            OrganizablePlayScreens.updateEntryNbt(nbtEntry, false);
            String type = nbtEntry.getStringOr("type", "");
            switch (type) {
                case "minecraft:world" -> {
                    int index = Collections.binarySearch(levels, new LevelSummary(null, LevelVersionInvoker.create(0, nbtEntry.getLongOr("lastPlayed", 0), null, 0, null, false), nbtEntry.getStringOr("name", ""), false, false, false, null));
                    if (index >= 0) {
                        WorldSelectionList.WorldListEntry worldEntry = ((WorldSelectionList) (Object) this).new WorldListEntry((WorldSelectionList) (Object) this, levels.get(index));
                        organizableplayscreens_worlds.put(worldEntry, folder);
                        folder.getWorldEntries().add(worldEntry);
                        levels.remove(index);
                    }
                }
                case OrganizablePlayScreens.MOD_ID + ":folder" -> {
                    SingleplayerFolderEntry folderEntry = new SingleplayerFolderEntry((SelectWorldScreen) screen, folder, nbtEntry.getStringOr("name", ""));
                    if (nbtEntry.getBooleanOr("current", false)) {
                        organizableplayscreens_currentFolder = folderEntry;
                    }
                    organizableplayscreens_fromNbt(folderEntry, nbtEntry, levels);
                    folder.getNonWorldEntries().add(folderEntry);
                }
                default -> folder.getNonWorldEntries().add(EntryType.get(Identifier.parse(type)).singleplayerEntry((SelectWorldScreen) screen, folder, nbtEntry.getStringOr("name", "")));
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
        for (WorldSelectionList.WorldListEntry oldWorldEntry : oldFolder.getWorldEntries()) {
            WorldSelectionList.WorldListEntry newWorldEntry = ((WorldSelectionList) (Object) this).new WorldListEntry((WorldSelectionList) (Object) this, ((WorldEntryAccessor) oldWorldEntry).getSummary());
            organizableplayscreens_worlds.put(newWorldEntry, newFolder);
            newFolder.getWorldEntries().add(newWorldEntry);
        }
        for (AbstractSingleplayerEntry oldNonWorldEntry : oldFolder.getNonWorldEntries()) {
            if (oldNonWorldEntry instanceof SingleplayerFolderEntry oldFolderEntry) {
                SingleplayerFolderEntry newFolderEntry = new SingleplayerFolderEntry((SelectWorldScreen) screen, newFolder, oldFolderEntry.getName());
                organizableplayscreens_fromFolder(newFolderEntry, oldFolderEntry, oldCurrentFolder);
                newFolder.getNonWorldEntries().add(newFolderEntry);
            } else {
                newFolder.getNonWorldEntries().add(oldNonWorldEntry.getType().singleplayerEntry((SelectWorldScreen) screen, newFolder, oldNonWorldEntry.getName()));
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
            CompoundTag nbtCompound = organizableplayscreens_toNbt(organizableplayscreens_rootFolder);
            Path runDirectory = minecraft.gameDirectory.toPath();
            Path tempFile = Files.createTempFile(runDirectory, "organizable_worlds", ".dat");
            NbtIo.write(nbtCompound, tempFile);
            Path backup = runDirectory.resolve("organizable_worlds.dat_old");
            Path file = runDirectory.resolve("organizable_worlds.dat");
            Util.safeReplaceFile(file, tempFile, backup);
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
    private CompoundTag organizableplayscreens_toNbt(SingleplayerFolderEntry folder) {
        ListTag nbtList = new ListTag();
        for (AbstractSingleplayerEntry nonWorldEntry : folder.getNonWorldEntries()) {
            CompoundTag nbtEntry = new CompoundTag();
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
        for (WorldSelectionList.WorldListEntry worldEntry : folder.getWorldEntries()) {
            CompoundTag nbtEntry = new CompoundTag();
            nbtEntry.putString("type", "minecraft:world");
            LevelSummary level = ((WorldEntryAccessor) worldEntry).getSummary();
            nbtEntry.putString("name", level.getLevelId());
            nbtEntry.putLong("lastPlayed", level.getLastPlayed());
            nbtList.add(nbtEntry);
        }
        CompoundTag nbtCompound = new CompoundTag();
        nbtCompound.put("entries", nbtList);
        return nbtCompound;
    }

    /**
     * Updates the displayed entries with specified search string.
     */
    @Inject(method = "fillLevels", at = @At("HEAD"), cancellable = true)
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
        scrollToEntry(getSelected());
    }

    /**
     * Updates the displayed entries with the stored search string.
     */
    @Unique
    private void organizableplayscreens_updateEntries() {
        organizableplayscreens_updateEntries(filter);
    }

    /**
     * Clears the displayed entries, sets {@link #organizableplayscreens_currentFolder currentFolder} to the folder that the selected entry is in if {@code search} is empty and the selected entry is a world entry, and displays all entries in {@link #organizableplayscreens_currentFolder currentFolder} or all entries in {@link #organizableplayscreens_worlds worlds} with the search filter applied if it is not empty.
     */
    @Unique
    private void organizableplayscreens_updateEntries(String search) {
        // Save the selected entry because clear entries sets selected to null
        WorldSelectionList.Entry selected = getSelected();
        super.clearEntries(); // Call clear entries on super to prevent closing entries

        if (search.isEmpty()) {
            if (selected instanceof WorldSelectionList.WorldListEntry worldEntry) {
                SingleplayerFolderEntry folderEntry = organizableplayscreens_worlds.get(worldEntry);
                if (folderEntry != null) {
                    organizableplayscreens_currentFolder = folderEntry;
                    ((SelectWorldScreen) screen).updateButtonStatus(null);
                }
            }
            organizableplayscreens_currentFolder.getNonWorldEntries().forEach(this::addEntry);
            organizableplayscreens_currentFolder.getWorldEntries().forEach(this::addEntry);
        } else {
            search = search.toLowerCase(Locale.ROOT);
            for (WorldSelectionList.WorldListEntry entry : organizableplayscreens_worlds.keySet()) {
                if (filterAccepts(search, ((WorldEntryAccessor) entry).getSummary())) {
                    addEntry(entry);
                }
            }
        }

        if (selected == null) {
            setScrollAmount(0);
        } else if (children().contains(selected)) {
            setSelected(selected);
            scrollToEntry(selected);
        }
        organizableplayscreens_updateCurrentPath(search);
        notifyListUpdated();
    }

    /**
     * Updates the path of {@link #organizableplayscreens_currentFolder currentFolder}.
     */
    @Unique
    public void organizableplayscreens_updateCurrentPath(String search) {
        // Update the path widget to search results if searching
        if (!search.isEmpty()) {
            organizableplayscreens_pathWidget.setMessage(Component.translatable("debug.options.search").withColor(0xFFA0A0A0));
            ((SelectWorldScreenAccessor) screen).invokeRepositionElements();
            return;
        }
        // Update the path widget to the full path if not searching
        List<String> path = new ArrayList<>();
        SingleplayerFolderEntry folder = organizableplayscreens_currentFolder;
        while (folder.getParent() != null) {
            path.add(folder.getName());
            folder = folder.getParent();
        }
        Collections.reverse(path);
        organizableplayscreens_pathWidget.setMessage(Component.literal(String.join(" > ", path)).withColor(0xFFA0A0A0));
        ((SelectWorldScreenAccessor) screen).invokeRepositionElements();
    }

    @Mixin(WorldSelectionList.WorldListEntry.class)
    public interface WorldEntryAccessor {
        @Accessor
        LevelSummary getSummary();

        @Accessor
        FaviconTexture getIcon();
    }
}
