package com.kevinthegreat.organizableplayscreens.gui.screen;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.gui.AbstractEntry;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;

import java.util.List;
import java.util.function.Function;

public class MultiplayerEditEntryScreen extends AbstractEditEntryScreen<ServerSelectionList, ServerSelectionList.Entry> {
    public MultiplayerEditEntryScreen(Screen parent, BooleanConsumer callback, Function<EntryType, AbstractEntry<ServerSelectionList, ServerSelectionList.Entry>> factory) {
        super(parent, callback, factory);
    }

    public MultiplayerEditEntryScreen(Screen parent, BooleanConsumer callback, AbstractEntry<ServerSelectionList, ServerSelectionList.Entry> entry) {
        super(parent, callback, entry);
    }

    @Override
    protected List<EntryType> getEntryTypes() {
        return EntryType.getMultiplayerEntryTypes();
    }
}
