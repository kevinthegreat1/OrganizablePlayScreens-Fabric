package com.kevinthegreat.organizableplayscreens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FolderEntry extends MultiplayerServerListWidget.Entry {
    private static final Text FOLDER_TEXT = Text.translatable("organizableplayscreens:folder.folder");
    private static final Identifier SERVER_SELECTION_TEXTURE = new Identifier("textures/gui/server_selection.png");
    private final MinecraftClient client;
    private final MultiplayerScreen screen;
    @NotNull
    private String name;
    private FolderEntry parent;
    @NotNull
    private final List<MultiplayerServerListWidget.Entry> entries;
    private final ButtonWidget buttonMoveInto;
    private long time;

    public FolderEntry(MultiplayerScreen screen, FolderEntry parent) {
        this(screen, parent, I18n.translate("organizableplayscreens:folder.newFolder"), new ArrayList<>());
    }

    public FolderEntry(MultiplayerScreen screen, FolderEntry parent, @NotNull String name) {
        this(screen, parent, name, new ArrayList<>());
    }

    public FolderEntry(MultiplayerScreen screen, FolderEntry parent, @NotNull String name, @NotNull List<MultiplayerServerListWidget.Entry> entries) {
        client = MinecraftClient.getInstance();
        this.screen = screen;
        this.parent = parent;
        this.name = name;
        this.entries = entries;
        buttonMoveInto = new ButtonWidget(0, 0, 20, 20, Text.of("+"), button -> {
            MultiplayerServerListWidget.Entry entry = screen.serverListWidget.getSelectedOrNull();
            if (entry != null) {
                if (entry instanceof FolderEntry folderEntry) {
                    folderEntry.parent = this;
                }
                entries.add(entry);
                ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_getCurrentEntries().remove(entry);
                ((MultiplayerServerListWidgetAccessor) screen.serverListWidget).organizableplayscreens_updateAndSave();
            }
        }, new ButtonWidget.TooltipSupplier() {
            private static final Text MOVE_ENTRY_INTO_TOOLTIP = Text.translatable("organizableplayscreens:folder.moveInto");

            @Override
            public void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
                if (button.isHovered()) {
                    screen.renderOrderedTooltip(matrices, MinecraftClient.getInstance().textRenderer.wrapLines(MOVE_ENTRY_INTO_TOOLTIP, screen.width / 2), mouseX, mouseY);
                }
            }

            @Override
            public void supply(Consumer<Text> consumer) {
                consumer.accept(MOVE_ENTRY_INTO_TOOLTIP);
            }
        });
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

    public void setParent(FolderEntry parent) {
        this.parent = parent;
    }

    public @NotNull List<MultiplayerServerListWidget.Entry> getEntries() {
        return entries;
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        client.textRenderer.draw(matrices, name, x + 32 + 3, y + 1, 0xffffff);
        client.textRenderer.draw(matrices, FOLDER_TEXT, x + 32 + 3, y + 12, 0x808080);
        if (client.options.getTouchscreen().getValue() || hovered) {
            RenderSystem.setShaderTexture(0, SERVER_SELECTION_TEXTURE);
            DrawableHelper.fill(matrices, x, y, x + 32, y + 32, 0xa0909090);
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
        buttonMoveInto.x = x + entryWidth - 30;
        buttonMoveInto.y = y + 6;
        buttonMoveInto.render(matrices, mouseX, mouseY, tickDelta);
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

    public void updateButtonActivationStates() {
        MultiplayerServerListWidget.Entry entry = screen.serverListWidget.getSelectedOrNull();
        buttonMoveInto.active = entry != null && entry != this;
    }

    @Override
    public Text getNarration() {
        return Text.translatable("narrator.select", name);
    }
}
