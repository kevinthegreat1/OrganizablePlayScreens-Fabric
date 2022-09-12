package com.kevinthegreat.organizableplayscreens;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class FolderEntry extends MultiplayerServerListWidget.Entry {
    private final MinecraftClient minecraftClient;
    private final MultiplayerScreen screen;
    private final String name;
    private final FolderEntry parent;
    private final List<MultiplayerServerListWidget.Entry> entries;
    private long time;

    public FolderEntry(MultiplayerScreen screen, FolderEntry parent, String name) {
        this(screen, parent, name, new ArrayList<>());
    }

    public FolderEntry(MultiplayerScreen screen, FolderEntry parent, String name, List<MultiplayerServerListWidget.Entry> entries) {
        minecraftClient = MinecraftClient.getInstance();
        this.screen = screen;
        this.parent = parent;
        this.name = name;
        this.entries = entries;
    }

    public String getName() {
        return name;
    }

    public FolderEntry getParent() {
        return parent;
    }

    public List<MultiplayerServerListWidget.Entry> getEntries() {
        return entries;
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        minecraftClient.textRenderer.draw(matrices, name, x + 32 + 3, y + 1, 16777215);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        screen.select(this);
        if (Util.getMeasuringTimeMs() - time < 250) {
            ((MultiplayerScreenAccessor) screen).organizableplayscreens_openFolder(this);
        }
        time = Util.getMeasuringTimeMs();
        return false;
    }

    @Override
    public Text getNarration() {
        return Text.translatable("narrator.select", name);
    }
}
