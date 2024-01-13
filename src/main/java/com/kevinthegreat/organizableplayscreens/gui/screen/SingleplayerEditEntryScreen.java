package com.kevinthegreat.organizableplayscreens.gui.screen;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.gui.AbstractEntry;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.WorldListWidget;

import java.util.List;
import java.util.function.Function;

public class SingleplayerEditEntryScreen extends AbstractEditEntryScreen<WorldListWidget, WorldListWidget.Entry> {
    public SingleplayerEditEntryScreen(Screen parent, BooleanConsumer callback, Function<EntryType, AbstractEntry<WorldListWidget, WorldListWidget.Entry>> factory) {
        super(parent, callback, factory);
    }

    public SingleplayerEditEntryScreen(Screen parent, BooleanConsumer callback, AbstractEntry<WorldListWidget, WorldListWidget.Entry> entry) {
        super(parent, callback, entry);
    }

    @Override
    protected List<EntryType> getEntryTypes() {
        return EntryType.getSingleplayerEntryTypes();
    }
}
