package com.kevinthegreat.organizableplayscreens;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SingleplayerFolderEntry extends WorldListWidget.Entry implements Mutable<String> {
    private final SelectWorldScreen screen;
    @NotNull
    private String name;
    /**
     * The parent of this folder.
     */
    private SingleplayerFolderEntry parent;
    /**
     * All folder entries in this folder.
     */
    @NotNull
    private final List<SingleplayerFolderEntry> folderEntries;
    /**
     * All world entries in this folder.
     */
    @NotNull
    private final List<WorldListWidget.WorldEntry> worldEntries;
    /**
     * This button moves the selected entry into this folder.
     */
    private final ButtonWidget buttonMoveInto;
    /**
     * Used to detect double-clicking.
     */
    private long time;

    public SingleplayerFolderEntry(SelectWorldScreen screen, SingleplayerFolderEntry parent) {
        this(screen, parent, I18n.translate("organizableplayscreens:folder.newFolder"), new ArrayList<>(), new ArrayList<>());
    }

    public SingleplayerFolderEntry(SelectWorldScreen screen, SingleplayerFolderEntry parent, @NotNull String name) {
        this(screen, parent, name, new ArrayList<>(), new ArrayList<>());
    }

    public SingleplayerFolderEntry(SelectWorldScreen screen, SingleplayerFolderEntry parent, @NotNull String name, @NotNull List<SingleplayerFolderEntry> folderEntries, @NotNull List<WorldListWidget.WorldEntry> worldEntries) {
        this.screen = screen;
        this.parent = parent;
        this.name = name;
        this.folderEntries = folderEntries;
        this.worldEntries = worldEntries;
        buttonMoveInto = new ButtonWidget(0, 0, 20, 20, Text.of("+"), button -> {
            WorldListWidget.Entry entry = screen.levelList.getSelectedOrNull();
            if (entry instanceof WorldListWidget.WorldEntry worldEntry) {
                ((WorldListWidgetAccessor) screen.levelList).organizableplayscreens_getWorlds().put(worldEntry, this);
                worldEntries.add(worldEntry);
                OrganizablePlayScreens.sortWorldEntries(worldEntries);
                ((WorldListWidgetAccessor) screen.levelList).organizableplayscreens_getCurrentWorldEntries().remove(worldEntry);
            } else if (entry instanceof SingleplayerFolderEntry folderEntry) {
                folderEntry.parent = this;
                folderEntries.add(folderEntry);
                ((WorldListWidgetAccessor) screen.levelList).organizableplayscreens_getCurrentFolderEntries().remove(folderEntry);
            }
            screen.levelList.setSelected(null);
            ((WorldListWidgetAccessor) screen.levelList).organizableplayscreens_updateAndSave();
        }, OrganizablePlayScreens.MOVE_ENTRY_INTO_TOOLTIP_SUPPLIER);
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getValue() {
        return name;
    }

    @Override
    public void setValue(@NotNull String name) {
        this.name = name;
    }

    public SingleplayerFolderEntry getParent() {
        return parent;
    }

    public void setParent(SingleplayerFolderEntry parent) {
        this.parent = parent;
    }

    public @NotNull List<SingleplayerFolderEntry> getFolderEntries() {
        return folderEntries;
    }

    public @NotNull List<WorldListWidget.WorldEntry> getWorldEntries() {
        return worldEntries;
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        OrganizablePlayScreens.renderFolderEntry(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta, name, ((WorldListWidgetAccessor) screen.levelList).organizableplayscreens_getCurrentFolderEntries().size(), buttonMoveInto);
    }

    /**
     * Handles key presses for this folder.
     * <p>
     * The folder is opened if the key is {@link GLFW#GLFW_KEY_ENTER}. Then, the folder is shifted down or up if {@link GLFW#GLFW_KEY_LEFT_SHIFT} and {@link GLFW#GLFW_KEY_DOWN} or {@link GLFW#GLFW_KEY_UP} are pressed, and it is valid to shift.
     *
     * @param keyCode the key code of the key that was pressed
     * @return whether the key press has been consumed (prevents further processing or not)
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            screen.levelList.setSelected(this);
            ((WorldListWidgetAccessor) screen.levelList).organizableplayscreens_setCurrentFolder(this);
            return true;
        } else if (Screen.hasShiftDown()) {
            int i = ((WorldListWidgetAccessor) screen.levelList).organizableplayscreens_getCurrentFolderEntries().indexOf(this);
            if (i != -1 && (keyCode == GLFW.GLFW_KEY_DOWN && i < ((WorldListWidgetAccessor) screen.levelList).organizableplayscreens_getCurrentFolderEntries().size() - 1 || keyCode == GLFW.GLFW_KEY_UP && i > 0)) {
                swapEntries(i, keyCode == GLFW.GLFW_KEY_DOWN ? i + 1 : i - 1);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Handles mouse clicks for this folder.
     * <p>
     * First, calls mouse click on {@link #buttonMoveInto}.
     * Then, checks for click on the open and swap buttons.
     * Finally, handles double-clicking.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (buttonMoveInto.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        int i = ((WorldListWidgetAccessor) screen.levelList).organizableplayscreens_getCurrentFolderEntries().indexOf(this);
        double d = mouseX - (double) screen.levelList.getRowLeft();
        double e = mouseY - (double) screen.levelList.getRowTop(i);
        if (d <= 32) {
            if (d < 32 && d > 16) {
                screen.levelList.setSelected(this);
                ((WorldListWidgetAccessor) screen.levelList).organizableplayscreens_setCurrentFolder(this);
                return true;
            }
            if (d < 16 && e < 16 && i > 0) {
                swapEntries(i, i - 1);
                return true;
            }
            if (d < 16 && e > 16 && i < ((WorldListWidgetAccessor) screen.levelList).organizableplayscreens_getCurrentFolderEntries().size() - 1) {
                swapEntries(i, i + 1);
                return true;
            }
        }

        screen.levelList.setSelected(this);
        if (Util.getMeasuringTimeMs() - time < 250) {
            ((WorldListWidgetAccessor) screen.levelList).organizableplayscreens_setCurrentFolder(this);
        }
        time = Util.getMeasuringTimeMs();
        return false;
    }

    /**
     * Swaps the entries at {@code i} and {@code j} and updates and saves the entries.
     *
     * @param i the index of the selected entry
     * @param j the index of the entry to swap with
     * @see WorldListWidgetAccessor#organizableplayscreens_swapEntries(int, int) swapEntries(int, int)
     */
    private void swapEntries(int i, int j) {
        ((WorldListWidgetAccessor) screen.levelList).organizableplayscreens_swapEntries(i, j);
    }

    /**
     * Updates the activation state of {@link #buttonMoveInto}.
     */
    public void updateButtonStates() {
        WorldListWidget.Entry entry = screen.levelList.getSelectedOrNull();
        buttonMoveInto.active = entry != null && entry != this;
    }

    @Override
    public Text getNarration() {
        return Text.translatable("narrator.select", name);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
