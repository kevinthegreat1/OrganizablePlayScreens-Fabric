package com.kevinthegreat.organizableplayscreens.gui.screen;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.gui.AbstractEntry;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;

import java.util.List;
import java.util.function.Function;

public class MultiplayerEditEntryScreen extends AbstractEditEntryScreen<MultiplayerServerListWidget.Entry> {
    public MultiplayerEditEntryScreen(Screen parent, BooleanConsumer callback, Function<EntryType, AbstractEntry<MultiplayerServerListWidget.Entry>> factory) {
        super(parent, callback, factory);
    }

    public MultiplayerEditEntryScreen(Screen parent, BooleanConsumer callback, AbstractEntry<MultiplayerServerListWidget.Entry> entry) {
        super(parent, callback, entry);
    }

    @Override
    protected List<EntryType> getEntryTypes() {
        return EntryType.getMultiplayerEntryTypes();
    }
}
