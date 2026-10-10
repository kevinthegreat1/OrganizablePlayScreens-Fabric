package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.SelectableEntry;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.Nullable;

/**
 * An abstract entry with a name and type.
 */
public interface AbstractEntry<T extends ObjectSelectionList<E>, E extends ObjectSelectionList.Entry<E>> extends EntryWithIcon, Mutable<String>, SelectableEntry {
    Minecraft client = Minecraft.getInstance();
    Identifier JOIN_TEXTURE = Identifier.parse("server_list/join");
    Identifier JOIN_HIGHLIGHTED_TEXTURE = Identifier.parse("server_list/join_highlighted");
    Identifier MOVE_UP_TEXTURE = Identifier.parse("server_list/move_up");
    Identifier MOVE_UP_HIGHLIGHTED_TEXTURE = Identifier.parse("server_list/move_up_highlighted");
    Identifier MOVE_DOWN_TEXTURE = Identifier.parse("server_list/move_down");
    Identifier MOVE_DOWN_HIGHLIGHTED_TEXTURE = Identifier.parse("server_list/move_down_highlighted");

    EntryType getType();

    @Override
    default String getValue() {
        return getName();
    }

    String getName();

    @Override
    default void setValue(String s) {
        setName(s);
    }

    void setName(String name);

    default void entrySelectionConfirmed(T listWidget) {
        client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    /**
     * Updates the button states of the given buttons in the screen when this entry is selected.
     *
     * @param selectButton   the select/confirm/open button
     * @param editButton     the edit button
     * @param deleteButton   the delete button
     * @param recreateButton the recreate button, only present in the singleplayer screen
     */
    default void updateScreenButtonStates(Button selectButton, Button editButton, Button deleteButton, @Nullable Button recreateButton) {
        selectButton.active = false;
        editButton.active = true;
        deleteButton.active = true;
        if (recreateButton != null) {
            recreateButton.active = false;
        }
    }

    /**
     * Updates the button states of the buttons under this entry when an entry is selected.
     *
     * @param entry the selected entry
     */
    default void updateButtonStates(@Nullable E entry) {
    }

    void render(GuiGraphicsExtractor context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize);

    default void renderIcon(GuiGraphicsExtractor context, int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int depth) {
        getCustomIcon().ifPresent(identifier -> context.blit(RenderPipelines.GUI_TEXTURED, identifier, x, y, 0, 0, width, height, 32, 32, 32, 32));
    }

    /**
     * Renders a section entry with the given parameters.
     */
    default void renderSectionEntry(GuiGraphicsExtractor context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, String name, int listSize) {
        context.text(client.font, name, x + 32 + 3, y + 1, 0xFFFFFFFF);
        context.text(client.font, EntryType.SECTION.text(), x + 32 + 3, y + 12, 0xFF808080);
        renderEntry(context, index, y, x, mouseX, mouseY, hovered, listSize, false);
    }

    /**
     * Renders a separator entry with the given parameters.
     */
    default void renderSeparatorEntry(GuiGraphicsExtractor context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, String name, int listSize) {
        context.text(client.font, "--------------------------------------", x + 32 + 3, y + 12, 0xFFFFFFFF);
        context.text(client.font, name, x + 32 + 3, y + 23, 0xFF808080);
        renderEntry(context, index, y, x, mouseX, mouseY, hovered, listSize, false);
    }

    /**
     * Renders the move entry and open entry buttons.
     *
     * @param renderOpenButton whether to render the open entry button.
     * @see net.minecraft.client.gui.screens.multiplayer.ServerSelectionList.OnlineServerEntry#extractContent(GuiGraphicsExtractor, int, int, boolean, float)
     */
    default void renderEntry(GuiGraphicsExtractor context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, int listSize, boolean renderOpenButton) {
        if (hovered) {
            context.fill(x, y, x + 32, y + 32, 0xa0909090);
            int relX = mouseX - x;
            int relY = mouseY - y;
            if (renderOpenButton) {
                if (mouseOverRightHalf(relX, relY, 32)) {
                    context.blitSprite(RenderPipelines.GUI_TEXTURED, JOIN_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                } else {
                    context.blitSprite(RenderPipelines.GUI_TEXTURED, JOIN_TEXTURE, x, y, 32, 32);
                }
            }
            if (index > 0) {
                if (mouseOverTopLeftQuarter(relX, relY, 32)) {
                    context.blitSprite(RenderPipelines.GUI_TEXTURED, MOVE_UP_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                } else {
                    context.blitSprite(RenderPipelines.GUI_TEXTURED, MOVE_UP_TEXTURE, x, y, 32, 32);
                }
            }
            if (index < listSize - 1) {
                if (mouseOverBottomLeftQuarter(relX, relY, 32)) {
                    context.blitSprite(RenderPipelines.GUI_TEXTURED, MOVE_DOWN_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                } else {
                    context.blitSprite(RenderPipelines.GUI_TEXTURED, MOVE_DOWN_TEXTURE, x, y, 32, 32);
                }
            }
        }
    }
}
