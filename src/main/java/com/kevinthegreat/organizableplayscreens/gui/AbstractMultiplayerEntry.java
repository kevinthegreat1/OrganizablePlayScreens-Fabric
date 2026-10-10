package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.FaviconTextureAccessor;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.JoinMultiplayerScreenAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
    @NotNull
    private final FaviconTexture customIconTexture;

    /**
     * Creates a new entry with the default name.
     *
     * @param screen the screen this entry is on
     * @param parent the parent folder of this entry
     * @param type   the type of this entry
     */
    public AbstractMultiplayerEntry(@NotNull JoinMultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent, @NotNull EntryType type) {
        this(screen, parent, type, I18n.get("organizableplayscreens:entry.new", type.text().getString()), null);
    }

    /**
     * Creates a new entry with the specified name.
     *
     * @param screen the screen this entry is on
     * @param parent the parent folder of this entry
     * @param type   the type of this entry
     * @param name   the name of this entry
     */
    public AbstractMultiplayerEntry(@NotNull JoinMultiplayerScreen screen, @Nullable MultiplayerFolderEntry parent, @NotNull EntryType type, @NotNull String name, @Nullable NativeImage customIconImage) {
        this.screen = screen;
        this.parent = parent;
        this.type = type;
        this.name = name;
        this.customIconTexture = OrganizablePlayScreens.uploadCustomIcon(this, customIconImage);
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
    public boolean matches(@NotNull ServerSelectionList.Entry entry) {
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
    public @NotNull FaviconTexture getCustomIconTexture() {
        return customIconTexture;
    }

    @Override
    public Optional<Identifier> getCustomIcon() {
        return ((FaviconTextureAccessor) customIconTexture).getTexture() != null ? Optional.of(customIconTexture.textureLocation()) : Optional.empty();
    }

    @Override
    public final void extractContent(@NotNull GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        render(context, ((JoinMultiplayerScreenAccessor) screen).getServerSelectionList().children().indexOf(this), getContentY(), getContentX(), mouseX, mouseY, hovered, tickDelta, name, ((JoinMultiplayerScreenAccessor) screen).getServerSelectionList().organizableplayscreens_getCurrentEntries().size());
    }

    /**
     * Handles key presses for this folder.
     * <p>
     * The folder is shifted down or up if {@link InputConstants#KEY_LSHIFT} and {@link InputConstants#KEY_DOWN} or {@link InputConstants#KEY_UP} are pressed, and it is valid to shift.
     *
     * @return whether the key press has been consumed (prevents further processing or not)
     * @see ServerSelectionList.OnlineServerEntry#keyPressed(KeyEvent)
     */
    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.isSelection()) {
            join();
            return true;
        }

        if (input.hasShiftDown()) {
            ServerSelectionList serverListWidget = ((JoinMultiplayerScreenAccessor) screen).getServerSelectionList();
            int i = serverListWidget.organizableplayscreens_getCurrentEntries().indexOf(this);
            if (i == -1) {
                return true;
            }
            if (input.isDown() && i < serverListWidget.organizableplayscreens_getCurrentEntries().size() - 1 || input.isUp() && i > 0) {
                swapEntries(i, input.isDown() ? i + 1 : i - 1);
                return true;
            }
        }
        return super.keyPressed(input);
    }

    /**
     * Handles mouse clicks for this folder.
     * <p>
     * Checks for click on the open and swap buttons, and handles double-clicking.
     *
     * @see ServerSelectionList.OnlineServerEntry#mouseClicked(MouseButtonEvent, boolean)
     */
    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        ServerSelectionList serverListWidget = ((JoinMultiplayerScreenAccessor) screen).getServerSelectionList();
        int relX = (int) click.x() - getContentX();
        int relY = (int) click.y() - getContentY();
        if (mouseOverRightHalf(relX, relY, 32)) {
            serverListWidget.setSelected(this);
            join();
            return true;
        }
        int i = serverListWidget.organizableplayscreens_getCurrentEntries().indexOf(this);
        if (i > 0 && mouseOverTopLeftQuarter(relX, relY, 32)) {
            swapEntries(i, i - 1);
            return true;
        }
        if (i < serverListWidget.organizableplayscreens_getCurrentEntries().size() - 1 && mouseOverBottomLeftQuarter(relX, relY, 32)) {
            swapEntries(i, i + 1);
            return true;
        }

        serverListWidget.setSelected(this);
        if (doubled) {
            join();
        }
        return super.mouseClicked(click, doubled);
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
    public @NotNull Component getNarration() {
        return Component.translatable("narrator.select", name);
    }

    @Override
    public void close() {
        if (!customIconTexture.isClosed()) {
            customIconTexture.close();
        }
    }
}
