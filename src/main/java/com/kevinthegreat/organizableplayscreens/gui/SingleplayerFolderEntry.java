package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.mixin.WorldListWidgetMixin;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.SelectWorldScreenAccessor;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldIcon;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SingleplayerFolderEntry extends AbstractSingleplayerEntry implements AbstractFolderEntry<WorldListWidget, WorldListWidget.Entry> {
    /**
     * All non-world entries in this folder.
     */
    @NotNull
    private final List<AbstractSingleplayerEntry> nonWorldEntries;
    /**
     * All world entries in this folder.
     */
    @NotNull
    private final List<WorldListWidget.WorldEntry> worldEntries;
    /**
     * This button moves the selected entry into this folder.
     */
    private final ButtonWidget buttonMoveInto;

    public SingleplayerFolderEntry(@NotNull SelectWorldScreen screen, @Nullable SingleplayerFolderEntry parent) {
        this(screen, parent, I18n.translate("organizableplayscreens:entry.new", EntryType.FOLDER.text().getString()), new ArrayList<>(), new ArrayList<>());
    }

    public SingleplayerFolderEntry(@NotNull SelectWorldScreen screen, @Nullable SingleplayerFolderEntry parent, @NotNull String name) {
        this(screen, parent, name, new ArrayList<>(), new ArrayList<>());
    }

    public SingleplayerFolderEntry(@NotNull SelectWorldScreen screen, @Nullable SingleplayerFolderEntry parent, @NotNull String name, @NotNull List<AbstractSingleplayerEntry> nonWorldEntries, @NotNull List<WorldListWidget.WorldEntry> worldEntries) {
        super(screen, parent, EntryType.FOLDER, name);
        this.nonWorldEntries = nonWorldEntries;
        this.worldEntries = worldEntries;
        buttonMoveInto = ButtonWidget.builder(Text.of("+"), button -> {
            WorldListWidget levelList = ((SelectWorldScreenAccessor) screen).getLevelList();
            WorldListWidget.Entry entry = levelList.getSelectedOrNull();
            if (entry instanceof WorldListWidget.WorldEntry worldEntry) {
                levelList.organizableplayscreens_getWorlds().put(worldEntry, this);
                worldEntries.add(worldEntry);
                OrganizablePlayScreens.sortWorldEntries(worldEntries);
                levelList.organizableplayscreens_getCurrentWorldEntries().remove(worldEntry);
            } else if (entry instanceof AbstractSingleplayerEntry nonWorldEntry) {
                nonWorldEntry.parent = this;
                nonWorldEntries.add(nonWorldEntry);
                levelList.organizableplayscreens_getCurrentNonWorldEntries().remove(nonWorldEntry);
            }
            levelList.setSelected(null);
            levelList.organizableplayscreens_updateAndSave();
        }).width(20).tooltip(OrganizablePlayScreens.MOVE_ENTRY_INTO_TOOLTIP).build();
    }

    public @NotNull List<AbstractSingleplayerEntry> getNonWorldEntries() {
        return nonWorldEntries;
    }

    public @NotNull List<WorldListWidget.WorldEntry> getWorldEntries() {
        return worldEntries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Identifier> getIcons() {
        return worldEntries.stream()
                .map(WorldListWidgetMixin.WorldEntryAccessor.class::cast)
                .map(WorldListWidgetMixin.WorldEntryAccessor::getIcon)
                .map(WorldIcon::getTextureId)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ButtonWidget getButtonMoveInto() {
        return buttonMoveInto;
    }

    @Override
    public void entrySelectionConfirmed(WorldListWidget levelList) {
        super.entrySelectionConfirmed(levelList);
        levelList.organizableplayscreens_setCurrentFolder(this);
    }

    /**
     * Handles mouse clicks for this folder.
     * <p>
     * Calls mouse click on {@link #buttonMoveInto} and {@link AbstractSingleplayerEntry#mouseClicked(Click, boolean)}
     */
    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (buttonMoveInto.mouseClicked(click, doubled)) {
            return true;
        }
        return super.mouseClicked(click, doubled);
    }
}
