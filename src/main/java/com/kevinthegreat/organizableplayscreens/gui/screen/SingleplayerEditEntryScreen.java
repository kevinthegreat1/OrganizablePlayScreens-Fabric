package com.kevinthegreat.organizableplayscreens.gui.screen;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.gui.AbstractEntry;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;

import java.util.List;
import java.util.function.Function;

public class SingleplayerEditEntryScreen extends AbstractEditEntryScreen<WorldSelectionList, WorldSelectionList.Entry> {
    public SingleplayerEditEntryScreen(Screen parent, BooleanConsumer callback, Function<EntryType, AbstractEntry<WorldSelectionList, WorldSelectionList.Entry>> factory) {
        super(parent, callback, factory);
    }

    public SingleplayerEditEntryScreen(Screen parent, BooleanConsumer callback, AbstractEntry<WorldSelectionList, WorldSelectionList.Entry> entry) {
        super(parent, callback, entry);
    }

    @Override
    protected List<EntryType> getEntryTypes() {
        return EntryType.getSingleplayerEntryTypes();
    }
}
