package com.kevinthegreat.organizableplayscreens;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;

public class FolderEntry extends MultiplayerServerListWidget.Entry {
    private final MinecraftClient minecraftClient;
    private final String name;
    private final List<MultiplayerServerListWidget.Entry> entries;

    public List<MultiplayerServerListWidget.Entry> getEntries() {
        return entries;
    }

    public FolderEntry(String name, List<MultiplayerServerListWidget.Entry> entries) {
        this.name = name;
        this.entries = entries;
        minecraftClient = MinecraftClient.getInstance();
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        minecraftClient.textRenderer.draw(matrices, name, x + 32 + 3, y + 1, 16777215);
    }

    @Override
    public Text getNarration() {
        return Text.translatable("narrator.select", name);
    }
}
