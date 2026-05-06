package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.AbstractSelectionListInvoker;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.SelectWorldScreenAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractSingleplayerEntry extends WorldSelectionList.Entry implements AbstractEntry<WorldSelectionList, WorldSelectionList.Entry> {
    @NotNull
    protected final SelectWorldScreen screen;
    /**
     * The parent of this folder.
     */
    @Nullable
    protected SingleplayerFolderEntry parent;
    @NotNull
    protected final EntryType type;
    @NotNull
    protected String name;

    /**
     * Creates a new entry with the default name.
     *
     * @param screen the screen this entry is on
     * @param parent the parent folder of this entry
     * @param type   the type of this entry
     */
    public AbstractSingleplayerEntry(@NotNull SelectWorldScreen screen, @Nullable SingleplayerFolderEntry parent, @NotNull EntryType type) {
        this(screen, parent, type, I18n.get("organizableplayscreens:entry.new", type.text().getString()));
    }

    /**
     * Creates a new entry with the specified name.
     *
     * @param screen the screen this entry is on
     * @param parent the parent folder of this entry
     * @param type   the type of this entry
     * @param name   the name of this entry
     */
    public AbstractSingleplayerEntry(@NotNull SelectWorldScreen screen, @Nullable SingleplayerFolderEntry parent, @NotNull EntryType type, @NotNull String name) {
        this.screen = screen;
        this.parent = parent;
        this.type = type;
        this.name = name;
    }

    public @Nullable SingleplayerFolderEntry getParent() {
        return parent;
    }

    public void setParent(@Nullable SingleplayerFolderEntry parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull EntryType getType() {
        return type;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public void setName(@NotNull String name) {
        this.name = name;
    }

    @Override
    public final void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        render(context, ((SelectWorldScreenAccessor) screen).getList().children().indexOf(this), getContentY(), getContentX(), mouseX, mouseY, hovered, tickDelta, name, ((SelectWorldScreenAccessor) screen).getList().organizableplayscreens_getCurrentNonWorldEntries().size());
    }

    /**
     * Handles key presses for this folder.
     * <p>
     * The folder is opened if the key is {@link GLFW#GLFW_KEY_ENTER}. Then, the folder is shifted down or up if {@link GLFW#GLFW_KEY_LEFT_SHIFT} and {@link GLFW#GLFW_KEY_DOWN} or {@link GLFW#GLFW_KEY_UP} are pressed, and it is valid to shift.
     *
     * @return whether the key press has been consumed (prevents further processing or not)
     */
    @Override
    public boolean keyPressed(KeyEvent input) {
        WorldSelectionList levelList = ((SelectWorldScreenAccessor) screen).getList();
        if (input.isSelection()) {
            levelList.setSelected(this);
            entrySelectionConfirmed(levelList);
            return true;
        } else if (input.hasShiftDown()) {
            int i = levelList.organizableplayscreens_getCurrentNonWorldEntries().indexOf(this);
            if (i != -1 && (input.key() == GLFW.GLFW_KEY_DOWN && i < levelList.organizableplayscreens_getCurrentNonWorldEntries().size() - 1 || input.key() == GLFW.GLFW_KEY_UP && i > 0)) {
                swapEntries(i, input.key() == GLFW.GLFW_KEY_DOWN ? i + 1 : i - 1);
            }
            return true;
        }
        return super.keyPressed(input);
    }

    /**
     * Handles mouse clicks for this folder.
     * <p>
     * Checks for click on the open and swap buttons, and handles double-clicking.
     */
    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        WorldSelectionList levelList = ((SelectWorldScreenAccessor) screen).getList();
        int i = levelList.organizableplayscreens_getCurrentNonWorldEntries().indexOf(this);
        double d = click.x() - (double) levelList.getRowLeft();
        double e = click.y() - (double) ((AbstractSelectionListInvoker) levelList).rowTop(i);
        if (d <= 32) {
            if (d < 32 && d > 16) {
                levelList.setSelected(this);
                entrySelectionConfirmed(levelList);
                return true;
            }
            if (d < 16 && e < 16 && i > 0) {
                swapEntries(i, i - 1);
                return true;
            }
            if (d < 16 && e > 16 && i < levelList.organizableplayscreens_getCurrentNonWorldEntries().size() - 1) {
                swapEntries(i, i + 1);
                return true;
            }
        }

        levelList.setSelected(this);
        if (doubled) {
            entrySelectionConfirmed(levelList);
        }
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
        ((SelectWorldScreenAccessor) screen).getList().organizableplayscreens_swapEntries(i, j);
    }

    @Override
    public Component getNarration() {
        return Component.translatable("narrator.select", name);
    }
}
