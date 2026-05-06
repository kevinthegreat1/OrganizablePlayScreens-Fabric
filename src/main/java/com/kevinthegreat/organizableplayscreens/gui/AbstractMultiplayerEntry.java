package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.AbstractSelectionListInvoker;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.JoinMultiplayerScreenAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractMultiplayerEntry extends ServerSelectionList.Entry implements AbstractEntry<ServerSelectionList, ServerSelectionList.Entry> {
    @NotNull
    protected final JoinMultiplayerScreen screen;
    /**
     * The parent of this folder.
     */
    @Nullable
    protected MultiplayerFolderEntry parent;
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
    public AbstractMultiplayerEntry(@NotNull JoinMultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent, @NotNull EntryType type) {
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
    public AbstractMultiplayerEntry(@NotNull JoinMultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent, @NotNull EntryType type, @NotNull String name) {
        this.screen = screen;
        this.parent = parent;
        this.type = type;
        this.name = name;
    }

    public @Nullable MultiplayerFolderEntry getParent() {
        return parent;
    }

    public void setParent(@Nullable MultiplayerFolderEntry parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull EntryType getType() {
        return type;
    }

    @Override
    public boolean matches(ServerSelectionList.Entry entry) {
        return entry instanceof AbstractMultiplayerEntry other && other.getType() == getType();
    }

    @Override
    public void join() {
        entrySelectionConfirmed(((JoinMultiplayerScreenAccessor) screen).getServerSelectionList());
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
        render(context, ((JoinMultiplayerScreenAccessor) screen).getServerSelectionList().children().indexOf(this), getContentY(), getContentX(), mouseX, mouseY, hovered, tickDelta, name, ((JoinMultiplayerScreenAccessor) screen).getServerSelectionList().organizableplayscreens_getCurrentEntries().size());
    }

    /**
     * Handles key presses for this folder.
     * <p>
     * The folder is shifted down or up if {@link org.lwjgl.glfw.GLFW#GLFW_KEY_LEFT_SHIFT} and {@link GLFW#GLFW_KEY_DOWN} or {@link GLFW#GLFW_KEY_UP} are pressed, and it is valid to shift.
     *
     * @return whether the key press has been consumed (prevents further processing or not)
     */
    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.hasShiftDown()) {
            ServerSelectionList serverListWidget = ((JoinMultiplayerScreenAccessor) screen).getServerSelectionList();
            int i = serverListWidget.organizableplayscreens_getCurrentEntries().indexOf(this);
            if (i == -1) {
                return true;
            }
            if (input.key() == GLFW.GLFW_KEY_DOWN && i < serverListWidget.organizableplayscreens_getCurrentEntries().size() - 1 || input.key() == GLFW.GLFW_KEY_UP && i > 0) {
                swapEntries(i, input.key() == GLFW.GLFW_KEY_DOWN ? i + 1 : i - 1);
                return true;
            }
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
        ServerSelectionList serverListWidget = ((JoinMultiplayerScreenAccessor) screen).getServerSelectionList();
        int i = serverListWidget.organizableplayscreens_getCurrentEntries().indexOf(this);
        double d = click.x() - (double) serverListWidget.getRowLeft();
        double e = click.y() - (double) ((AbstractSelectionListInvoker) serverListWidget).rowTop(i);
        if (d <= 32) {
            if (d < 32 && d > 16) {
                serverListWidget.setSelected(this);
                join();
                return true;
            }
            if (d < 16 && e < 16 && i > 0) {
                swapEntries(i, i - 1);
                return true;
            }
            if (d < 16 && e > 16 && i < serverListWidget.organizableplayscreens_getCurrentEntries().size() - 1) {
                swapEntries(i, i + 1);
                return true;
            }
        }

        serverListWidget.setSelected(this);
        if (doubled) {
            join();
        }
        return false;
    }

    /**
     * Swaps the entries at {@code i} and {@code j} and updates and saves the entries.
     *
     * @param i the index of the selected entry
     * @param j the index of the entry to swap with
     * @see com.kevinthegreat.organizableplayscreens.gui.MultiplayerServerListWidgetAccessor#organizableplayscreens_swapEntries(int, int) swapEntries(int, int)
     */
    private void swapEntries(int i, int j) {
        ((JoinMultiplayerScreenAccessor) screen).getServerSelectionList().organizableplayscreens_swapEntries(i, j);
    }

    @Override
    public Component getNarration() {
        return Component.translatable("narrator.select", name);
    }
}
