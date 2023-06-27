package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.mixin.accessor.EntryListWidgetInvoker;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.SelectWorldScreenAccessor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractSingleplayerEntry extends WorldListWidget.Entry implements Mutable<String> {
    @NotNull
    protected final SelectWorldScreen screen;
    /**
     * The parent of this folder.
     */
    @Nullable
    protected SingleplayerFolderEntry parent;
    @NotNull
    protected String name;
    /**
     * Used to detect double-clicking.
     */
    private long time;

    public AbstractSingleplayerEntry(@NotNull SelectWorldScreen screen, @Nullable SingleplayerFolderEntry parent, @NotNull String name) {
        this.screen = screen;
        this.parent = parent;
        this.name = name;
    }

    public @Nullable SingleplayerFolderEntry getParent() {
        return parent;
    }

    public void setParent(@Nullable SingleplayerFolderEntry parent) {
        this.parent = parent;
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

    protected void entrySelected(WorldListWidgetAccessor levelList) {
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
        WorldListWidget levelList = ((SelectWorldScreenAccessor) screen).getLevelList();
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            levelList.setSelected(this);
            entrySelected((WorldListWidgetAccessor) levelList);
            return true;
        } else if (Screen.hasShiftDown()) {
            int i = ((WorldListWidgetAccessor) levelList).organizableplayscreens_getCurrentNonWorldEntries().indexOf(this);
            if (i != -1 && (keyCode == GLFW.GLFW_KEY_DOWN && i < ((WorldListWidgetAccessor) levelList).organizableplayscreens_getCurrentNonWorldEntries().size() - 1 || keyCode == GLFW.GLFW_KEY_UP && i > 0)) {
                swapEntries(i, keyCode == GLFW.GLFW_KEY_DOWN ? i + 1 : i - 1);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Handles mouse clicks for this folder.
     * <p>
     * Checks for click on the open and swap buttons, and handles double-clicking.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        WorldListWidget levelList = ((SelectWorldScreenAccessor) screen).getLevelList();
        int i = ((WorldListWidgetAccessor) levelList).organizableplayscreens_getCurrentNonWorldEntries().indexOf(this);
        double d = mouseX - (double) levelList.getRowLeft();
        double e = mouseY - (double) ((EntryListWidgetInvoker) levelList).rowTop(i);
        if (d <= 32) {
            if (d < 32 && d > 16) {
                levelList.setSelected(this);
                entrySelected((WorldListWidgetAccessor) levelList);
                return true;
            }
            if (d < 16 && e < 16 && i > 0) {
                swapEntries(i, i - 1);
                return true;
            }
            if (d < 16 && e > 16 && i < ((WorldListWidgetAccessor) levelList).organizableplayscreens_getCurrentNonWorldEntries().size() - 1) {
                swapEntries(i, i + 1);
                return true;
            }
        }

        levelList.setSelected(this);
        if (Util.getMeasuringTimeMs() - time < 250) {
            entrySelected((WorldListWidgetAccessor) levelList);
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
        ((WorldListWidgetAccessor) ((SelectWorldScreenAccessor) screen).getLevelList()).organizableplayscreens_swapEntries(i, j);
    }

    public void updateButtonStates() {
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
