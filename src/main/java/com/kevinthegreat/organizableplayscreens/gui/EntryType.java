package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.mojang.datafixers.util.Function3;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.function.BiFunction;

public class EntryType {
    public static final Map<String, EntryType> ENTRY_TYPE_MAP = Map.of(
            EntryType.FOLDER.id(), EntryType.FOLDER,
            EntryType.SECTION.id(), EntryType.SECTION,
            EntryType.SEPARATOR.id(), EntryType.SEPARATOR
    );
    public static final EntryType FOLDER = new EntryType(OrganizablePlayScreens.MOD_ID + ":folder", OrganizablePlayScreens.MOD_ID + ":folder.folder", MultiplayerFolderEntry::new, MultiplayerFolderEntry::new, SingleplayerFolderEntry::new, SingleplayerFolderEntry::new);
    public static final EntryType SECTION = new EntryType(OrganizablePlayScreens.MOD_ID + ":section", OrganizablePlayScreens.MOD_ID + ":entry.section", MultiplayerSectionEntry::new, MultiplayerSectionEntry::new, SingleplayerSectionEntry::new, SingleplayerSectionEntry::new);
    public static final EntryType SEPARATOR = new EntryType(OrganizablePlayScreens.MOD_ID + ":separator", OrganizablePlayScreens.MOD_ID + ":entry.separator", MultiplayerSeparatorEntry::new, MultiplayerSeparatorEntry::new, SingleplayerSeparatorEntry::new, SingleplayerSeparatorEntry::new);
    private final String id;
    private final Text text;
    private final BiFunction<MultiplayerScreen, MultiplayerFolderEntry, AbstractMultiplayerEntry> multiplayerNewEntryFactory;
    private final Function3<MultiplayerScreen, MultiplayerFolderEntry, String, AbstractMultiplayerEntry> multiplayerEntryFactory;
    private final BiFunction<SelectWorldScreen, SingleplayerFolderEntry, AbstractSingleplayerEntry> singleplayerNewEntryFactory;
    private final Function3<SelectWorldScreen, SingleplayerFolderEntry, String, AbstractSingleplayerEntry> singleplayerEntryFactory;

    public EntryType(String id, String key, BiFunction<MultiplayerScreen, MultiplayerFolderEntry, AbstractMultiplayerEntry> multiplayerNewEntryFactory, Function3<MultiplayerScreen, MultiplayerFolderEntry, String, AbstractMultiplayerEntry> multiplayerEntryFactory, BiFunction<SelectWorldScreen, SingleplayerFolderEntry, AbstractSingleplayerEntry> singleplayerNewEntryFactory, Function3<SelectWorldScreen, SingleplayerFolderEntry, String, AbstractSingleplayerEntry> singleplayerEntryFactory) {
        this.id = id;
        this.text = Text.translatable(key);
        this.multiplayerNewEntryFactory = multiplayerNewEntryFactory;
        this.multiplayerEntryFactory = multiplayerEntryFactory;
        this.singleplayerNewEntryFactory = singleplayerNewEntryFactory;
        this.singleplayerEntryFactory = singleplayerEntryFactory;
    }

    public static EntryType ofMultiplayer(String id, String key, BiFunction<MultiplayerScreen, MultiplayerFolderEntry, AbstractMultiplayerEntry> multiplayerNewEntryFactory, Function3<MultiplayerScreen, MultiplayerFolderEntry, String, AbstractMultiplayerEntry> multiplayerEntryFactory) {
        return new EntryType(id, key, multiplayerNewEntryFactory, multiplayerEntryFactory, null, null);
    }

    public static EntryType ofSingleplayer(String id, String key, BiFunction<SelectWorldScreen, SingleplayerFolderEntry, AbstractSingleplayerEntry> singleplayerNewEntryFactory, Function3<SelectWorldScreen, SingleplayerFolderEntry, String, AbstractSingleplayerEntry> singleplayerEntryFactory) {
        return new EntryType(id, key, null, null, singleplayerNewEntryFactory, singleplayerEntryFactory);
    }

    public String id() {
        return id;
    }

    public Text text() {
        return text;
    }

    public AbstractMultiplayerEntry multiplayerEntry(MultiplayerScreen screen, MultiplayerFolderEntry folder) {
        return multiplayerNewEntryFactory.apply(screen, folder);
    }

    public AbstractMultiplayerEntry multiplayerEntry(MultiplayerScreen screen, MultiplayerFolderEntry folder, String name) {
        return multiplayerEntryFactory.apply(screen, folder, name);
    }

    public AbstractSingleplayerEntry singleplayerEntry(SelectWorldScreen screen, SingleplayerFolderEntry folder) {
        return singleplayerNewEntryFactory.apply(screen, folder);
    }

    public AbstractSingleplayerEntry singleplayerEntry(SelectWorldScreen screen, SingleplayerFolderEntry folder, String name) {
        return singleplayerEntryFactory.apply(screen, folder, name);
    }
}
