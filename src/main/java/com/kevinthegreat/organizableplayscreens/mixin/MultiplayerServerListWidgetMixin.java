package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.compatibility.Compatibility;
import com.kevinthegreat.organizableplayscreens.gui.AbstractMultiplayerEntry;
import com.kevinthegreat.organizableplayscreens.gui.MultiplayerFolderEntry;
import com.kevinthegreat.organizableplayscreens.gui.MultiplayerServerListWidgetAccessor;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.MultiplayerScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.screen.world.WorldIcon;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
     * A comparator used to sort and search for server entries by address and then name.
     */
    @Unique
    private static final Comparator<MultiplayerServerListWidget.ServerEntry> serverEntryComparator = Comparator.comparing((MultiplayerServerListWidget.ServerEntry entry) -> entry.getServer().address).thenComparing(entry -> entry.getServer().name);

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
     * @see com.kevinthegreat.organizableplayscreens.mixin.MultiplayerScreenMixin#organizableplayscreens_pathWidget
     */
    @SuppressWarnings("JavadocReference")
    @Unique
    private TextWidget organizableplayscreens_pathWidget;

    public MultiplayerServerListWidgetMixin(MinecraftClient minecraftClient, int i, int j, int k, int l) {
        super(minecraftClient, i, j, k, l);
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
    public void organizableplayscreens_setPathWidget(TextWidget pathWidget) {
        organizableplayscreens_pathWidget = pathWidget;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void organizableplayscreens_setCurrentFolder(@NotNull MultiplayerFolderEntry folderEntry) {
        organizableplayscreens_currentFolder = folderEntry;
        setSelected(null);
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
            NbtCompound nbtCompound = NbtIo.read(client.runDirectory.toPath().resolve("organizable_servers.dat"));
            List<MultiplayerServerListWidget.ServerEntry> serversSorted = new ArrayList<>(servers);
            serversSorted.sort(serverEntryComparator);
            if (nbtCompound != null) {
                organizableplayscreens_fromNbt(organizableplayscreens_rootFolder, nbtCompound, serversSorted);
            }
            for (MultiplayerServerListWidget.ServerEntry serverEntry : servers) {
                if (serversSorted.contains(serverEntry)) {
                    organizableplayscreens_currentFolder.getEntries().add(serverEntry);
                }
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
            Path runDirectory = client.runDirectory.toPath();
            Path tempFile = Files.createTempFile(runDirectory, "organizable_servers", ".dat");
            NbtIo.write(nbtCompound, tempFile);
            Path backup = runDirectory.resolve("organizable_servers.dat_old");
            Path file = runDirectory.resolve("organizable_servers.dat");
            Util.backupAndReplace(file, tempFile, backup);
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
     * Reads the folders and servers from {@code nbtCompound} and the vanilla server list and adds them to {@code folder}.
     *
     * @param folder      the folder to add the entries to
     * @param nbtCompound the NBT compound to read from
     */
    @Unique
    private void organizableplayscreens_fromNbt(MultiplayerFolderEntry folder, NbtCompound nbtCompound, List<MultiplayerServerListWidget.ServerEntry> serversSorted) {
        NbtList nbtList = nbtCompound.getListOrEmpty("entries");
        folder.getEntries().clear();
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtEntry = nbtList.getCompoundOrEmpty(i);
            OrganizablePlayScreens.updateEntryNbt(nbtEntry, true);
            String type = nbtEntry.getString("type", "");
            switch (type) {
                case "minecraft:server" -> {
                    if (!nbtEntry.getBoolean("hidden", false)) {
                        int index = Collections.binarySearch(serversSorted, ServerEntryAccessor.create((MultiplayerServerListWidget) (Object) this, screen, new ServerInfo(nbtEntry.getString("name", ""), nbtEntry.getString("ip", ""), null)), serverEntryComparator);
                        if (index >= 0) {
                            folder.getEntries().add(serversSorted.remove(index));
                        }
                    }
                }
                case OrganizablePlayScreens.MOD_ID + ":folder" -> {
                    MultiplayerFolderEntry folderEntry = new MultiplayerFolderEntry(screen, folder, nbtEntry.getString("name", ""));
                    if (nbtEntry.getBoolean("current", false)) {
                        organizableplayscreens_currentFolder = folderEntry;
                    }
                    organizableplayscreens_fromNbt(folderEntry, nbtEntry, serversSorted);
                    folder.getEntries().add(folderEntry);
                }
                default -> folder.getEntries().add(EntryType.get(Identifier.of(type)).multiplayerEntry(screen, folder, nbtEntry.getString("name", "")));
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
    private NbtCompound organizableplayscreens_toNbt(MultiplayerFolderEntry folder) {
        NbtList nbtList = new NbtList();
        for (MultiplayerServerListWidget.Entry entry : folder.getEntries()) {
            if (entry instanceof MultiplayerServerListWidget.ServerEntry serverEntry) {
                NbtCompound nbtEntry = new NbtCompound();
                nbtEntry.putString("type", "minecraft:server");
                nbtEntry.putString("ip", serverEntry.getServer().address);
                nbtEntry.putString("name", serverEntry.getServer().name);
                nbtEntry.putBoolean("hidden", false);
                nbtList.add(nbtEntry);
            } else if (entry instanceof AbstractMultiplayerEntry nonServerEntry) {
                NbtCompound nbtEntry = new NbtCompound();
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
        scrollTo(getSelectedOrNull());
    }

    /**
     * Clears the displayed entries and displays all entries in {@link #organizableplayscreens_currentFolder}, {@link #scanningEntry}, and {@link #lanServers}.
     */
    @Inject(method = "updateEntries", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_updateEntries(CallbackInfo ci) {
        if (Compatibility.essential_preventMultiplayerFeatures()) {
            return;
        }
        clearEntries();
        organizableplayscreens_currentFolder.getEntries().forEach(this::addEntry);
        addEntry(scanningEntry);
        lanServers.forEach(this::addEntry);
        if (getSelectedOrNull() == null) {
            setScrollY(0);
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
        organizableplayscreens_pathWidget.setMessage(Text.of(String.join(" > ", path)));
        ((MultiplayerScreenAccessor) screen).getLayout().refreshPositions(); // Only refresh the layout positions instead of calling MultiplayerScreen#refreshWidgetPositions to avoid activating other mixins to prevent NPEs as this can run before all buttons are initialized.
    }

    @Mixin(MultiplayerServerListWidget.ServerEntry.class)
    public interface ServerEntryAccessor {
        @Accessor
        WorldIcon getIcon();

        @Accessor
        byte[] getFavicon();

        @Accessor
        void setFavicon(byte[] favicon);

        @SuppressWarnings("unused")
        @Invoker("<init>")
        static MultiplayerServerListWidget.ServerEntry create(MultiplayerServerListWidget serverListWidget, MultiplayerScreen screen, ServerInfo server) {
            throw new IllegalStateException("Mixin invoker failed to apply");
        }

        @Invoker
        void invokeUpdate();

        @Invoker
        boolean invokeUploadFavicon(byte[] favicon);
    }
}
