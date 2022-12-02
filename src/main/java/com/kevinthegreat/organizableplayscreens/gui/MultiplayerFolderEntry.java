package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.mixin.EntryListWidgetInvoker;
import com.kevinthegreat.organizableplayscreens.mixin.MultiplayerScreenAccessor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
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

public class MultiplayerFolderEntry extends MultiplayerServerListWidget.Entry implements Mutable<String> {
    private final MultiplayerScreen screen;
    @NotNull
    private String name;
    /**
     * The parent of this folder.
     */
    private MultiplayerFolderEntry parent;
    /**
     * All entries in this folder.
     */
    @NotNull
    private final List<MultiplayerServerListWidget.Entry> entries;
    /**
     * This button moves the selected entry into this folder.
     */
    private final ButtonWidget buttonMoveInto;
    /**
     * Used to detect double-clicking.
     */
    private long time;

    public MultiplayerFolderEntry(MultiplayerScreen screen, MultiplayerFolderEntry parent) {
        this(screen, parent, I18n.translate("organizableplayscreens:folder.newFolder"), new ArrayList<>());
    }

    public MultiplayerFolderEntry(MultiplayerScreen screen, MultiplayerFolderEntry parent, @NotNull String name) {
        this(screen, parent, name, new ArrayList<>());
    }

    public MultiplayerFolderEntry(MultiplayerScreen screen, MultiplayerFolderEntry parent, @NotNull String name, @NotNull List<MultiplayerServerListWidget.Entry> entries) {
        this.screen = screen;
        this.parent = parent;
        this.name = name;
        this.entries = entries;
        buttonMoveInto = ButtonWidget.builder(Text.of("+"), button -> {
            MultiplayerServerListWidget serverListWidget = ((MultiplayerScreenAccessor) screen).getServerListWidget();
            MultiplayerServerListWidget.Entry entry = serverListWidget.getSelectedOrNull();
            if (entry != null) {
                if (entry instanceof MultiplayerFolderEntry folderEntry) {
                    folderEntry.parent = this;
                }
                entries.add(entry);
                ((MultiplayerServerListWidgetAccessor) serverListWidget).organizableplayscreens_getCurrentEntries().remove(entry);
                ((MultiplayerServerListWidgetAccessor) serverListWidget).organizableplayscreens_updateAndSave();
            }
        }).width(20).tooltip(OrganizablePlayScreens.MOVE_ENTRY_INTO_TOOLTIP).build();
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

    public MultiplayerFolderEntry getParent() {
        return parent;
    }

    public void setParent(MultiplayerFolderEntry parent) {
        this.parent = parent;
    }

    public @NotNull List<MultiplayerServerListWidget.Entry> getEntries() {
        return entries;
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        OrganizablePlayScreens.renderFolderEntry(matrices, index, y, x, mouseX, mouseY, hovered, tickDelta, name, ((MultiplayerServerListWidgetAccessor) ((MultiplayerScreenAccessor) screen).getServerListWidget()).organizableplayscreens_getCurrentEntries().size(), buttonMoveInto);
    }

    /**
     * Handles key presses for this folder.
     * <p>
     * The folder is shifted down or up if {@link GLFW#GLFW_KEY_LEFT_SHIFT} and {@link GLFW#GLFW_KEY_DOWN} or {@link GLFW#GLFW_KEY_UP} are pressed, and it is valid to shift.
     *
     * @param keyCode the key code of the key that was pressed
     * @return whether the key press has been consumed (prevents further processing or not)
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasShiftDown()) {
            MultiplayerServerListWidget serverListWidget = ((MultiplayerScreenAccessor) screen).getServerListWidget();
            int i = ((MultiplayerServerListWidgetAccessor) serverListWidget).organizableplayscreens_getCurrentEntries().indexOf(this);
            if (i == -1) {
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN && i < ((MultiplayerServerListWidgetAccessor) serverListWidget).organizableplayscreens_getCurrentEntries().size() - 1 || keyCode == GLFW.GLFW_KEY_UP && i > 0) {
                swapEntries(i, keyCode == GLFW.GLFW_KEY_DOWN ? i + 1 : i - 1);
                return true;
            }
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
        MultiplayerServerListWidget serverListWidget = ((MultiplayerScreenAccessor) screen).getServerListWidget();
        int i = ((MultiplayerServerListWidgetAccessor) serverListWidget).organizableplayscreens_getCurrentEntries().indexOf(this);
        double d = mouseX - (double) serverListWidget.getRowLeft();
        double e = mouseY - (double) ((EntryListWidgetInvoker) serverListWidget).rowTop(i);
        if (d <= 32) {
            if (d < 32 && d > 16) {
                screen.select(this);
                screen.connect();
                return true;
            }
            if (d < 16 && e < 16 && i > 0) {
                swapEntries(i, i - 1);
                return true;
            }
            if (d < 16 && e > 16 && i < ((MultiplayerServerListWidgetAccessor) serverListWidget).organizableplayscreens_getCurrentEntries().size() - 1) {
                swapEntries(i, i + 1);
                return true;
            }
        }

        screen.select(this);
        if (Util.getMeasuringTimeMs() - time < 250) {
            screen.connect();
        }
        time = Util.getMeasuringTimeMs();
        return false;
    }

    /**
     * Swaps the entries at {@code i} and {@code j} and updates and saves the entries.
     *
     * @param i the index of the selected entry
     * @param j the index of the entry to swap with
     * @see MultiplayerServerListWidgetAccessor#organizableplayscreens_swapEntries(int, int) swapEntries(int, int)
     */
    private void swapEntries(int i, int j) {
        ((MultiplayerServerListWidgetAccessor) ((MultiplayerScreenAccessor) screen).getServerListWidget()).organizableplayscreens_swapEntries(i, j);
    }

    /**
     * Updates the activation state of {@link #buttonMoveInto}.
     */
    public void updateButtonStates() {
        MultiplayerServerListWidget.Entry entry = ((MultiplayerScreenAccessor) screen).getServerListWidget().getSelectedOrNull();
        buttonMoveInto.active = entry != null && entry != this;
    }

    @Override
    public Text getNarration() {
        return Text.translatable("narrator.select", name);
    }
}
