package com.kevinthegreat.organizableplayscreens;

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
    private MultiplayerFolderEntry parent;
    @NotNull
    private final List<MultiplayerServerListWidget.Entry> entries;
    private final ButtonWidget buttonMoveInto;
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
        buttonMoveInto = new ButtonWidget(0, 0, 20, 20, Text.of("+"), button -> {
            MultiplayerServerListWidget.Entry entry = screen.serverListWidget.getSelectedOrNull();
            if (entry != null) {
                if (entry instanceof MultiplayerFolderEntry folderEntry) {
                    folderEntry.parent = this;
                }
                entries.add(entry);
                ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_getCurrentEntries().remove(entry);
                ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_updateAndSave();
            }
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
        OrganizablePlayScreens.renderFolderEntry(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta, name, ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_getCurrentEntries().size(), buttonMoveInto);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasShiftDown()) {
            int i = ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_getCurrentEntries().indexOf(this);
            if (i == -1) {
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN && i < ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_getCurrentEntries().size() - 1 || keyCode == GLFW.GLFW_KEY_UP && i > 0) {
                swapEntries(i, keyCode == GLFW.GLFW_KEY_DOWN ? i + 1 : i - 1);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (buttonMoveInto.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        int i = ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_getCurrentEntries().indexOf(this);
        double d = mouseX - (double) screen.serverListWidget.getRowLeft();
        double e = mouseY - (double) screen.serverListWidget.getRowTop(i);
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
            if (d < 16 && e > 16 && i < ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_getCurrentEntries().size() - 1) {
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

    private void swapEntries(int i, int j) {
        ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_swapEntries(i, j);
    }

    public void updateButtonStates() {
        MultiplayerServerListWidget.Entry entry = screen.serverListWidget.getSelectedOrNull();
        buttonMoveInto.active = entry != null && entry != this;
    }

    @Override
    public Text getNarration() {
        return Text.translatable("narrator.select", name);
    }
}
