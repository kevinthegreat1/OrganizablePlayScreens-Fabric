package com.kevinthegreat.organizableplayscreens.api;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.gui.*;
import com.mojang.datafixers.util.Function3;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Represents a type of entry.
 */
public class EntryType {
    /**
     * A map of all entry types by their id. Used to deserialize entry types from json.
     */
    private static final Map<Identifier, EntryType> ENTRY_TYPE_MAP = new HashMap<>();
    /**
     * A list of all multiplayer entry types.
     */
    private static final List<EntryType> MULTIPLAYER_ENTRY_TYPES = new ArrayList<>();
    /**
     * A list of all singleplayer entry types.
     */
    private static final List<EntryType> SINGLEPLAYER_ENTRY_TYPES = new ArrayList<>();
    public static final EntryType FOLDER = register(new Identifier(OrganizablePlayScreens.MOD_ID, "folder"), Text.translatable(OrganizablePlayScreens.MOD_ID + ":folder.folder"), MultiplayerFolderEntry::new, MultiplayerFolderEntry::new, SingleplayerFolderEntry::new, SingleplayerFolderEntry::new);
    public static final EntryType SECTION = register(new Identifier(OrganizablePlayScreens.MOD_ID, "section"), Text.translatable(OrganizablePlayScreens.MOD_ID + ":entry.section"), MultiplayerSectionEntry::new, MultiplayerSectionEntry::new, SingleplayerSectionEntry::new, SingleplayerSectionEntry::new);
    public static final EntryType SEPARATOR = register(new Identifier(OrganizablePlayScreens.MOD_ID, "separator"), Text.translatable(OrganizablePlayScreens.MOD_ID + ":entry.separator"), MultiplayerSeparatorEntry::new, MultiplayerSeparatorEntry::new, SingleplayerSeparatorEntry::new, SingleplayerSeparatorEntry::new);
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

    private EntryType(Identifier id, Text text, BiFunction<MultiplayerScreen, MultiplayerFolderEntry, AbstractMultiplayerEntry> multiplayerNewEntryFactory, Function3<MultiplayerScreen, MultiplayerFolderEntry, String, AbstractMultiplayerEntry> multiplayerEntryFactory, BiFunction<SelectWorldScreen, SingleplayerFolderEntry, AbstractSingleplayerEntry> singleplayerNewEntryFactory, Function3<SelectWorldScreen, SingleplayerFolderEntry, String, AbstractSingleplayerEntry> singleplayerEntryFactory) {
        this.id = id;
        this.text = text;
        this.multiplayerNewEntryFactory = multiplayerNewEntryFactory;
        this.multiplayerEntryFactory = multiplayerEntryFactory;
        this.singleplayerNewEntryFactory = singleplayerNewEntryFactory;
        this.singleplayerEntryFactory = singleplayerEntryFactory;
    }

    /**
     * Creates and registers a new entry type for both multiplayer and singleplayer.
     *
     * @param id                          the identifier of the entry type
     * @param text                        the name of the entry type
     * @param multiplayerNewEntryFactory  the factory used to create a new multiplayer entry of the specific type with a default name
     * @param multiplayerEntryFactory     the factory used to create a multiplayer entry of the specific type with a given name
     * @param singleplayerNewEntryFactory the factory used to create a new singleplayer entry of the specific type with a default name
     * @param singleplayerEntryFactory    the factory used to create a singleplayer entry of the specific type with a given name
     */
    public static EntryType register(@NotNull Identifier id, @NotNull Text text, BiFunction<MultiplayerScreen, MultiplayerFolderEntry, AbstractMultiplayerEntry> multiplayerNewEntryFactory, Function3<MultiplayerScreen, MultiplayerFolderEntry, String, AbstractMultiplayerEntry> multiplayerEntryFactory, BiFunction<SelectWorldScreen, SingleplayerFolderEntry, AbstractSingleplayerEntry> singleplayerNewEntryFactory, Function3<SelectWorldScreen, SingleplayerFolderEntry, String, AbstractSingleplayerEntry> singleplayerEntryFactory) {
        EntryType entryType = new EntryType(id, text, multiplayerNewEntryFactory, multiplayerEntryFactory, singleplayerNewEntryFactory, singleplayerEntryFactory);
        ENTRY_TYPE_MAP.put(id, entryType);
        if (multiplayerNewEntryFactory != null && multiplayerEntryFactory != null) {
            MULTIPLAYER_ENTRY_TYPES.add(entryType);
        }
        if (singleplayerNewEntryFactory != null && singleplayerEntryFactory != null) {
            SINGLEPLAYER_ENTRY_TYPES.add(entryType);
        }
        return entryType;
    }

    /**
     * Creates and registers a new entry type for multiplayer only.
     *
     * @param id                         the identifier of the entry type
     * @param text                       the name of the entry type
     * @param multiplayerNewEntryFactory the factory used to create a new multiplayer entry of the specific type with a default name
     * @param multiplayerEntryFactory    the factory used to create a multiplayer entry of the specific type with a given name
     * @return the entry type
     */
    public static EntryType registerMultiplayer(@NotNull Identifier id, @NotNull Text text, @NotNull BiFunction<MultiplayerScreen, MultiplayerFolderEntry, AbstractMultiplayerEntry> multiplayerNewEntryFactory, @NotNull Function3<MultiplayerScreen, MultiplayerFolderEntry, String, AbstractMultiplayerEntry> multiplayerEntryFactory) {
        return register(id, text, multiplayerNewEntryFactory, multiplayerEntryFactory, null, null);
    }

    /**
     * Creates and registers a new entry type for singleplayer only.
     *
     * @param id                          the identifier of the entry type
     * @param text                        the name of the entry type
     * @param singleplayerNewEntryFactory the factory used to create a new singleplayer entry of the specific type with a default name
     * @param singleplayerEntryFactory    the factory used to create a singleplayer entry of the specific type with a given name
     * @return the entry type
     */
    public static EntryType registerSingleplayer(@NotNull Identifier id, @NotNull Text text, @NotNull BiFunction<SelectWorldScreen, SingleplayerFolderEntry, AbstractSingleplayerEntry> singleplayerNewEntryFactory, @NotNull Function3<SelectWorldScreen, SingleplayerFolderEntry, String, AbstractSingleplayerEntry> singleplayerEntryFactory) {
        return register(id, text, null, null, singleplayerNewEntryFactory, singleplayerEntryFactory);
    }

    public static EntryType get(Identifier id) {
        return ENTRY_TYPE_MAP.get(id);
    }

    public static List<EntryType> getMultiplayerEntryTypes() {
        return Collections.unmodifiableList(MULTIPLAYER_ENTRY_TYPES);
    }

    public static List<EntryType> getSingleplayerEntryTypes() {
        return Collections.unmodifiableList(SINGLEPLAYER_ENTRY_TYPES);
    }

    public Identifier id() {
        return id;
    }

    public Text text() {
        return text;
    }

    /**
     * Creates a new multiplayer entry of the specific type with a default name.
     *
     * @param screen the multiplayer screen
     * @param folder the folder the entry is in
     * @return the entry
     */
    public AbstractMultiplayerEntry multiplayerEntry(MultiplayerScreen screen, MultiplayerFolderEntry folder) {
        return multiplayerNewEntryFactory.apply(screen, folder);
    }

    /**
     * Creates a new multiplayer entry of the specific type with a given name.
     *
     * @param screen the multiplayer screen
     * @param folder the folder the entry is in
     * @param name   the name of the entry
     * @return the entry
     */
    public AbstractMultiplayerEntry multiplayerEntry(MultiplayerScreen screen, MultiplayerFolderEntry folder, String name) {
        return multiplayerEntryFactory.apply(screen, folder, name);
    }

    /**
     * Creates a new singleplayer entry of the specific type with a default name.
     *
     * @param screen the singleplayer screen
     * @param folder the folder the entry is in
     * @return the entry
     */
    public AbstractSingleplayerEntry singleplayerEntry(SelectWorldScreen screen, SingleplayerFolderEntry folder) {
        return singleplayerNewEntryFactory.apply(screen, folder);
    }

    /**
     * Creates a new singleplayer entry of the specific type with a given name.
     *
     * @param screen the singleplayer screen
     * @param folder the folder the entry is in
     * @param name   the name of the entry
     * @return the entry
     */
    public AbstractSingleplayerEntry singleplayerEntry(SelectWorldScreen screen, SingleplayerFolderEntry folder, String name) {
        return singleplayerEntryFactory.apply(screen, folder, name);
    }
}
