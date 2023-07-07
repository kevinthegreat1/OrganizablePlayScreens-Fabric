package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.mojang.datafixers.util.Function3;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Represents a type of entry.
 */
public class EntryType {
    public static final EntryType FOLDER = new EntryType(new Identifier(OrganizablePlayScreens.MOD_ID, "folder"), OrganizablePlayScreens.MOD_ID + ":folder.folder", MultiplayerFolderEntry::new, MultiplayerFolderEntry::new, SingleplayerFolderEntry::new, SingleplayerFolderEntry::new);
    public static final EntryType SECTION = new EntryType(new Identifier(OrganizablePlayScreens.MOD_ID, "section"), OrganizablePlayScreens.MOD_ID + ":entry.section", MultiplayerSectionEntry::new, MultiplayerSectionEntry::new, SingleplayerSectionEntry::new, SingleplayerSectionEntry::new);
    public static final EntryType SEPARATOR = new EntryType(new Identifier(OrganizablePlayScreens.MOD_ID, "separator"), OrganizablePlayScreens.MOD_ID + ":entry.separator", MultiplayerSeparatorEntry::new, MultiplayerSeparatorEntry::new, SingleplayerSeparatorEntry::new, SingleplayerSeparatorEntry::new);
    /**
     * A map of all entry types by their id. Used to deserialize entry types from json.
     */
    public static final Map<Identifier, EntryType> ENTRY_TYPE_MAP = Map.of(
            EntryType.FOLDER.id(), EntryType.FOLDER,
            EntryType.SECTION.id(), EntryType.SECTION,
            EntryType.SEPARATOR.id(), EntryType.SEPARATOR
    );
    /**
     * Identifier of the entry type.
     */
    private final Identifier id;
    /**
     * The text name of the entry type.
     */
    private final Text text;
    /**
     * Used to create a new multiplayer entry of the specific type with a default name.
     */
    private final BiFunction<MultiplayerScreen, MultiplayerFolderEntry, AbstractMultiplayerEntry> multiplayerNewEntryFactory;
    /**
     * Used to create a multiplayer entry of the specific type with a given name.
     */
    private final Function3<MultiplayerScreen, MultiplayerFolderEntry, String, AbstractMultiplayerEntry> multiplayerEntryFactory;
    /**
     * Used to create a new singleplayer entry of the specific type with a default name.
     */
    private final BiFunction<SelectWorldScreen, SingleplayerFolderEntry, AbstractSingleplayerEntry> singleplayerNewEntryFactory;
    /**
     * Used to create a singleplayer entry of the specific type with a given name.
     */
    private final Function3<SelectWorldScreen, SingleplayerFolderEntry, String, AbstractSingleplayerEntry> singleplayerEntryFactory;

    /**
     * Creates a new entry type for both multiplayer and singleplayer.
     * @param id the identifier of the entry type
     * @param key the translation key of the entry type
     * @param multiplayerNewEntryFactory the factory used to create a new multiplayer entry of the specific type with a default name
     * @param multiplayerEntryFactory the factory used to create a multiplayer entry of the specific type with a given name
     * @param singleplayerNewEntryFactory the factory used to create a new singleplayer entry of the specific type with a default name
     * @param singleplayerEntryFactory the factory used to create a singleplayer entry of the specific type with a given name
     */
    public EntryType(Identifier id, String key, BiFunction<MultiplayerScreen, MultiplayerFolderEntry, AbstractMultiplayerEntry> multiplayerNewEntryFactory, Function3<MultiplayerScreen, MultiplayerFolderEntry, String, AbstractMultiplayerEntry> multiplayerEntryFactory, BiFunction<SelectWorldScreen, SingleplayerFolderEntry, AbstractSingleplayerEntry> singleplayerNewEntryFactory, Function3<SelectWorldScreen, SingleplayerFolderEntry, String, AbstractSingleplayerEntry> singleplayerEntryFactory) {
        this.id = id;
        this.text = Text.translatable(key);
        this.multiplayerNewEntryFactory = multiplayerNewEntryFactory;
        this.multiplayerEntryFactory = multiplayerEntryFactory;
        this.singleplayerNewEntryFactory = singleplayerNewEntryFactory;
        this.singleplayerEntryFactory = singleplayerEntryFactory;
    }

    /**
     * Creates a new entry type for multiplayer only.
     * @param id the identifier of the entry type
     * @param key the translation key of the entry type
     * @param multiplayerNewEntryFactory the factory used to create a new multiplayer entry of the specific type with a default name
     * @param multiplayerEntryFactory the factory used to create a multiplayer entry of the specific type with a given name
     * @return the entry type
     */
    public static EntryType ofMultiplayer(Identifier id, String key, BiFunction<MultiplayerScreen, MultiplayerFolderEntry, AbstractMultiplayerEntry> multiplayerNewEntryFactory, Function3<MultiplayerScreen, MultiplayerFolderEntry, String, AbstractMultiplayerEntry> multiplayerEntryFactory) {
        return new EntryType(id, key, multiplayerNewEntryFactory, multiplayerEntryFactory, null, null);
    }

    /**
     * Creates a new entry type for singleplayer only.
     * @param id the identifier of the entry type
     * @param key the translation key of the entry type
     * @param singleplayerNewEntryFactory the factory used to create a new singleplayer entry of the specific type with a default name
     * @param singleplayerEntryFactory the factory used to create a singleplayer entry of the specific type with a given name
     * @return the entry type
     */
    public static EntryType ofSingleplayer(Identifier id, String key, BiFunction<SelectWorldScreen, SingleplayerFolderEntry, AbstractSingleplayerEntry> singleplayerNewEntryFactory, Function3<SelectWorldScreen, SingleplayerFolderEntry, String, AbstractSingleplayerEntry> singleplayerEntryFactory) {
        return new EntryType(id, key, null, null, singleplayerNewEntryFactory, singleplayerEntryFactory);
    }

    public Identifier id() {
        return id;
    }

    public Text text() {
        return text;
    }

    /**
     * Creates a new multiplayer entry of the specific type with a default name.
     * @param screen the multiplayer screen
     * @param folder the folder the entry is in
     * @return the entry
     */
    public AbstractMultiplayerEntry multiplayerEntry(MultiplayerScreen screen, MultiplayerFolderEntry folder) {
        return multiplayerNewEntryFactory.apply(screen, folder);
    }

    /**
     * Creates a new multiplayer entry of the specific type with a given name.
     * @param screen the multiplayer screen
     * @param folder the folder the entry is in
     * @param name the name of the entry
     * @return the entry
     */
    public AbstractMultiplayerEntry multiplayerEntry(MultiplayerScreen screen, MultiplayerFolderEntry folder, String name) {
        return multiplayerEntryFactory.apply(screen, folder, name);
    }

    /**
     * Creates a new singleplayer entry of the specific type with a default name.
     * @param screen the singleplayer screen
     * @param folder the folder the entry is in
     * @return the entry
     */
    public AbstractSingleplayerEntry singleplayerEntry(SelectWorldScreen screen, SingleplayerFolderEntry folder) {
        return singleplayerNewEntryFactory.apply(screen, folder);
    }

    /**
     * Creates a new singleplayer entry of the specific type with a given name.
     * @param screen the singleplayer screen
     * @param folder the folder the entry is in
     * @param name the name of the entry
     * @return the entry
     */
    public AbstractSingleplayerEntry singleplayerEntry(SelectWorldScreen screen, SingleplayerFolderEntry folder, String name) {
        return singleplayerEntryFactory.apply(screen, folder, name);
    }
}
