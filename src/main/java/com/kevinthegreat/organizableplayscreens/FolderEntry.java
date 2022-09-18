package com.kevinthegreat.organizableplayscreens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FolderEntry extends MultiplayerServerListWidget.Entry {
    private static final Text FOLDER_TEXT = Text.translatable("organizableplayscreens:folder.folder");
    private static final Identifier SERVER_SELECTION_TEXTURE = new Identifier("textures/gui/server_selection.png");
    private final MinecraftClient client;
    private final MultiplayerScreen screen;
    @NotNull
    private String name;
    private final FolderEntry parent;
    private final List<MultiplayerServerListWidget.Entry> entries;
    private long time;

    public FolderEntry(MultiplayerScreen screen, FolderEntry parent) {
        this(screen, parent, I18n.translate("organizableplayscreens:folder.newFolder"), new ArrayList<>());
    }

    public FolderEntry(MultiplayerScreen screen, FolderEntry parent, @NotNull String name) {
        this(screen, parent, name, new ArrayList<>());
    }

    public FolderEntry(MultiplayerScreen screen, FolderEntry parent, @NotNull String name, List<MultiplayerServerListWidget.Entry> entries) {
        client = MinecraftClient.getInstance();
        this.screen = screen;
        this.parent = parent;
        this.name = name;
        this.entries = entries;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public FolderEntry getParent() {
        return parent;
    }

    public List<MultiplayerServerListWidget.Entry> getEntries() {
        return entries;
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        client.textRenderer.draw(matrices, name, x + 32 + 3, y + 1, 16777215);
        client.textRenderer.draw(matrices, FOLDER_TEXT, x + 32 + 3, y + 12, 8421504);
        if (client.options.getTouchscreen().getValue() || hovered) {
            RenderSystem.setShaderTexture(0, SERVER_SELECTION_TEXTURE);
            DrawableHelper.fill(matrices, x, y, x + 32, y + 32, -1601138544);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            int o = mouseX - x;
            int p = mouseY - y;
            if (o < 32 && o > 16) {
                DrawableHelper.drawTexture(matrices, x, y, 0, 32, 32, 32, 256, 256);
            } else {
                DrawableHelper.drawTexture(matrices, x, y, 0, 0, 32, 32, 256, 256);
            }
            if (index > 0) {
                if (o < 16 && p < 16) {
                    DrawableHelper.drawTexture(matrices, x, y, 96, 32, 32, 32, 256, 256);
                } else {
                    DrawableHelper.drawTexture(matrices, x, y, 96, 0, 32, 32, 256, 256);
                }
            }
            if (index < ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_getCurrentEntries().size() - 1) {
                if (o < 16 && p > 16) {
                    DrawableHelper.drawTexture(matrices, x, y, 64, 32, 32, 32, 256, 256);
                } else {
                    DrawableHelper.drawTexture(matrices, x, y, 64, 0, 32, 32, 256, 256);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasShiftDown()) {
            int i = ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_getCurrentEntries().indexOf(this);
            if (i == -1) {
                return true;
            }
            if (keyCode == 264 && i < ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_getCurrentEntries().size() - 1 || keyCode == 265 && i > 0) {
                swapEntries(i, keyCode == 264 ? i + 1 : i - 1);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
            ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_setCurrentFolder(this);
        }
        time = Util.getMeasuringTimeMs();
        return false;
    }

    private void swapEntries(int i, int j) {
        ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_swapEntries(i, j);
    }

    @Override
    public Text getNarration() {
        return Text.translatable("narrator.select", name);
    }
}
