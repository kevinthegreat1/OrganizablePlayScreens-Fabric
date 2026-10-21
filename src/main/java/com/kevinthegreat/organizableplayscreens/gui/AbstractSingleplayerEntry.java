package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.FaviconTextureAccessor;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.SelectWorldScreenAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
    @NotNull
    private final FaviconTexture customIconTexture;

    /**
     * Creates a new entry with the default name.
     *
     * @param screen the screen this entry is on
     * @param parent the parent folder of this entry
     * @param type   the type of this entry
     */
    public AbstractSingleplayerEntry(@NotNull SelectWorldScreen screen, @Nullable SingleplayerFolderEntry parent, @NotNull EntryType type) {
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
    public AbstractSingleplayerEntry(@NotNull SelectWorldScreen screen, @Nullable SingleplayerFolderEntry parent, @NotNull EntryType type, @NotNull String name, @Nullable NativeImage customIconImage) {
        this.screen = screen;
        this.parent = parent;
        this.type = type;
        this.name = name;
        this.customIconTexture = OrganizablePlayScreens.uploadCustomIcon(this, customIconImage);
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
    public @NotNull FaviconTexture getCustomIconTexture() {
        return customIconTexture;
    }

    @Override
    public Optional<Identifier> getCustomIcon() {
        return ((FaviconTextureAccessor) customIconTexture).getTexture() != null ? Optional.of(customIconTexture.textureLocation()) : Optional.empty();
    }

    @Override
    public final void extractContent(@NotNull GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        render(context, ((SelectWorldScreenAccessor) screen).getList().children().indexOf(this), getContentY(), getContentX(), mouseX, mouseY, hovered, tickDelta, name, ((SelectWorldScreenAccessor) screen).getList().organizableplayscreens_getCurrentNonWorldEntries().size());
    }

    /**
     * Handles key presses for this folder.
     * <p>
     * The folder is opened if the key is {@link InputConstants#KEY_RETURN}. Then, the folder is shifted down or up if {@link InputConstants#KEY_LSHIFT} and {@link InputConstants#KEY_DOWN} or {@link InputConstants#KEY_UP} are pressed, and it is valid to shift.
     *
     * @return whether the key press has been consumed (prevents further processing or not)
     * @see WorldSelectionList.WorldListEntry#keyPressed(KeyEvent)
     */
    @Override
    public boolean keyPressed(KeyEvent input) {
        WorldSelectionList levelList = ((SelectWorldScreenAccessor) screen).getList();
        if (input.isSelection()) {
            levelList.setSelected(this);
            entrySelectionConfirmed(levelList);
            return true;
        }

        if (input.hasShiftDown()) {
            int i = levelList.organizableplayscreens_getCurrentNonWorldEntries().indexOf(this);
            if (i == -1) {
                return true;
            }
            if (input.isDown() && i < levelList.organizableplayscreens_getCurrentNonWorldEntries().size() - 1 || input.isUp() && i > 0) {
                swapEntries(i, input.isDown() ? i + 1 : i - 1);
            }
            return true;
        }
        return super.keyPressed(input);
    }

    /**
     * Handles mouse clicks for this folder.
     * <p>
     * Checks for click on the open and swap buttons, and handles double-clicking.
     *
     * @see WorldSelectionList.WorldListEntry#mouseClicked(MouseButtonEvent, boolean)
     */
    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        WorldSelectionList levelList = ((SelectWorldScreenAccessor) screen).getList();
        int relX = (int) click.x() - getContentX();
        int relY = (int) click.y() - getContentY();
        if (mouseOverRightHalf(relX, relY, 32)) {
            levelList.setSelected(this);
            entrySelectionConfirmed(levelList);
            return true;
        }
        int i = levelList.organizableplayscreens_getCurrentNonWorldEntries().indexOf(this);
        if (i > 0 && mouseOverTopLeftQuarter(relX, relY, 32)) {
            swapEntries(i, i - 1);
            return true;
        }
        if (i < levelList.organizableplayscreens_getCurrentNonWorldEntries().size() - 1 && mouseOverBottomLeftQuarter(relX, relY, 32)) {
            swapEntries(i, i + 1);
            return true;
        }

        levelList.setSelected(this);
        if (doubled) {
            entrySelectionConfirmed(levelList);
        }
        return super.mouseClicked(click, doubled);
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
