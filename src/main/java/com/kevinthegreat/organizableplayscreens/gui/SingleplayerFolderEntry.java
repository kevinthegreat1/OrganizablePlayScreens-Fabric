package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.SelectWorldScreenAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SingleplayerFolderEntry extends AbstractSingleplayerEntry {
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

    @SuppressWarnings("unused") // Used via reflection
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
                ((WorldListWidgetAccessor) levelList).organizableplayscreens_getWorlds().put(worldEntry, this);
                worldEntries.add(worldEntry);
                OrganizablePlayScreens.sortWorldEntries(worldEntries);
                ((WorldListWidgetAccessor) levelList).organizableplayscreens_getCurrentWorldEntries().remove(worldEntry);
            } else if (entry instanceof AbstractSingleplayerEntry nonWorldEntry) {
                nonWorldEntry.parent = this;
                nonWorldEntries.add(nonWorldEntry);
                ((WorldListWidgetAccessor) levelList).organizableplayscreens_getCurrentNonWorldEntries().remove(nonWorldEntry);
            }
            levelList.setSelected(null);
            ((WorldListWidgetAccessor) levelList).organizableplayscreens_updateAndSave();
        }).width(20).tooltip(OrganizablePlayScreens.MOVE_ENTRY_INTO_TOOLTIP).build();
    }

    public @NotNull List<AbstractSingleplayerEntry> getNonWorldEntries() {
        return nonWorldEntries;
    }

    public @NotNull List<WorldListWidget.WorldEntry> getWorldEntries() {
        return worldEntries;
    }

    @Override
    protected void render(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize) {
        OrganizablePlayScreens.renderFolderEntry(context, index, y, x, mouseX, mouseY, hovered, tickDelta, name, listSize, buttonMoveInto);
    }

    @Override
    protected void entrySelected(WorldListWidgetAccessor levelList) {
        levelList.organizableplayscreens_setCurrentFolder(this);
    }

    /**
     * Handles mouse clicks for this folder.
     * <p>
     * Calls mouse click on {@link #buttonMoveInto} and {@link AbstractSingleplayerEntry#mouseClicked(double, double, int)}
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (buttonMoveInto.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Updates the activation state of {@link #buttonMoveInto}.
     */
    @Override
    public void updateButtonStates() {
        WorldListWidget.Entry entry = ((SelectWorldScreenAccessor) screen).getLevelList().getSelectedOrNull();
        buttonMoveInto.active = entry != null && entry != this;
    }
}
