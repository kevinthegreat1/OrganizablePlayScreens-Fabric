package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * An abstract entry with a name and type.
 */
public interface AbstractEntry<T extends AlwaysSelectedEntryListWidget<E>, E extends AlwaysSelectedEntryListWidget.Entry<E>> extends Supplier<EntryType>, Mutable<String> {
    MinecraftClient client = MinecraftClient.getInstance();
    Identifier JOIN_TEXTURE = Identifier.of("server_list/join");
    Identifier JOIN_HIGHLIGHTED_TEXTURE = Identifier.of("server_list/join_highlighted");
    Identifier MOVE_UP_TEXTURE = Identifier.of("server_list/move_up");
    Identifier MOVE_UP_HIGHLIGHTED_TEXTURE = Identifier.of("server_list/move_up_highlighted");
    Identifier MOVE_DOWN_TEXTURE = Identifier.of("server_list/move_down");
    Identifier MOVE_DOWN_HIGHLIGHTED_TEXTURE = Identifier.of("server_list/move_down_highlighted");

    @Override
    default EntryType get() {
        return getType();
    }

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
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    /**
     * Updates the button states of the given buttons in the screen when this entry is selected.
     *
     * @param selectButton   the select/confirm/open button
     * @param editButton     the edit button
     * @param deleteButton   the delete button
     * @param recreateButton the recreate button, only present in the singleplayer screen
     */
    default void updateScreenButtonStates(ButtonWidget selectButton, ButtonWidget editButton, ButtonWidget deleteButton, @Nullable ButtonWidget recreateButton) {
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
    default void updateButtonStates(E entry) {
    }

    void render(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize);

    /**
     * Renders a folder entry with the given parameters.
     *
     * @param name           The name of the folder.
     * @param listSize       The size of the entry list that the folder is in.
     * @param buttonMoveInto The button to move the selected entry into the folder.
     */
    static void renderFolderEntry(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize, List<Identifier> icons, ButtonWidget buttonMoveInto) {
        switch (icons.size()) {
            case 0 -> {}
            case 1 -> context.drawTexture(RenderPipelines.GUI_TEXTURED, icons.getFirst(), x + 8, y + 8, 0, 0, 16, 16, 32, 32, 32, 32);
            case 2 -> {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, icons.getFirst(), x, y + 8, 0, 0, 16, 16, 32, 32, 32, 32);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, icons.getLast(), x + 16, y + 8, 0, 0, 16, 16, 32, 32, 32, 32);
            }
            case 3 -> {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, icons.get(0), x + 8, y, 0, 0, 16, 16, 32, 32, 32, 32);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, icons.get(1), x, y + 16, 0, 0, 16, 16, 32, 32, 32, 32);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, icons.get(2), x + 16, y + 16, 0, 0, 16, 16, 32, 32, 32, 32);
            }
            default -> {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, icons.get(0), x, y, 0, 0, 16, 16, 32, 32, 32, 32);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, icons.get(1), x + 16, y, 0, 0, 16, 16, 32, 32, 32, 32);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, icons.get(2), x, y + 16, 0, 0, 16, 16, 32, 32, 32, 32);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, icons.get(3), x + 16, y + 16, 0, 0, 16, 16, 32, 32, 32, 32);
            }
        }

        context.drawTextWithShadow(client.textRenderer, name, x + 32 + 3, y + 1, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer, EntryType.FOLDER.text(), x + 32 + 3, y + 12, 0xFF808080);
        renderEntry(context, index, y, x, mouseX, mouseY, hovered, listSize, true);
        OrganizablePlayScreensOptions options = OrganizablePlayScreens.getInstance().options;
        buttonMoveInto.setPosition(options.getValue(options.moveEntryIntoButtonX), y + options.moveEntryIntoButtonY.getValue());
        buttonMoveInto.render(context, mouseX, mouseY, tickDelta);
    }

    /**
     * Renders a section entry with the given parameters.
     */
    static void renderSectionEntry(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, String name, int listSize) {
        context.drawTextWithShadow(client.textRenderer, name, x + 32 + 3, y + 1, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer, EntryType.SECTION.text(), x + 32 + 3, y + 12, 0xFF808080);
        renderEntry(context, index, y, x, mouseX, mouseY, hovered, listSize, false);
    }

    /**
     * Renders a separator entry with the given parameters.
     */
    static void renderSeparatorEntry(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, String name, int listSize) {
        context.drawTextWithShadow(client.textRenderer, "--------------------------------------", x + 32 + 3, y + 12, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer, name, x + 32 + 3, y + 23, 0xFF808080);
        renderEntry(context, index, y, x, mouseX, mouseY, hovered, listSize, false);
    }

    /**
     * Renders the move entry and open entry buttons.
     *
     * @param renderOpenButton whether to render the open entry button.
     */
    static void renderEntry(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, int listSize, boolean renderOpenButton) {
        if (client.options.getTouchscreen().getValue() || hovered) {
            context.fill(x, y, x + 32, y + 32, 0xa0909090);
            int o = mouseX - x;
            int p = mouseY - y;
            if (renderOpenButton) {
                if (o < 32 && o > 16) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, JOIN_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                } else {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, JOIN_TEXTURE, x, y, 32, 32);
                }
            }
            if (index > 0) {
                if (o < 16 && p < 16) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, MOVE_UP_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                } else {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, MOVE_UP_TEXTURE, x, y, 32, 32);
                }
            }
            if (index < listSize - 1) {
                if (o < 16 && p > 16) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, MOVE_DOWN_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                } else {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, MOVE_DOWN_TEXTURE, x, y, 32, 32);
                }
            }
        }
    }
}
