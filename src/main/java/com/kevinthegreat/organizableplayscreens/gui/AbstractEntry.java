package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.Mutable;

import java.util.function.Supplier;

/**
 * An abstract entry with a name and type.
 */
public interface AbstractEntry extends Supplier<EntryType>, Mutable<String> {
    MinecraftClient client = MinecraftClient.getInstance();
    Identifier SERVER_SELECTION_TEXTURE = new Identifier("textures/gui/server_selection.png");

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

    void render(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize);

    /**
     * Renders a folder entry with the given parameters.
     *
     * @param name           The name of the folder.
     * @param listSize       The size of the entry list that the folder is in.
     * @param buttonMoveInto The button to move the selected entry into the folder.
     */
    static void renderFolderEntry(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize, ButtonWidget buttonMoveInto) {
        context.drawTextWithShadow(client.textRenderer, name, x + 32 + 3, y + 1, 0xFFFFFF);
        context.drawTextWithShadow(client.textRenderer, EntryType.FOLDER.text(), x + 32 + 3, y + 12, 0x808080);
        renderEntry(context, index, y, x, mouseX, mouseY, hovered, listSize, true);
        OrganizablePlayScreensOptions options = OrganizablePlayScreens.getInstance().options;
        buttonMoveInto.setPosition(options.getValue(options.moveEntryIntoButtonX), y + options.moveEntryIntoButtonY.getValue());
        buttonMoveInto.render(context, mouseX, mouseY, tickDelta);
    }

    /**
     * Renders a section entry with the given parameters.
     */
    static void renderSectionEntry(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, String name, int listSize) {
        context.drawTextWithShadow(client.textRenderer, name, x + 32 + 3, y + 1, 0xFFFFFF);
        context.drawTextWithShadow(client.textRenderer, EntryType.SECTION.text(), x + 32 + 3, y + 12, 0x808080);
        renderEntry(context, index, y, x, mouseX, mouseY, hovered, listSize, false);
    }

    /**
     * Renders a separator entry with the given parameters.
     */
    static void renderSeparatorEntry(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, String name, int listSize) {
        context.drawTextWithShadow(client.textRenderer, "--------------------------------------", x + 32 + 3, y + 12, 0xFFFFFF);
        context.drawTextWithShadow(client.textRenderer, name, x + 32 + 3, y + 23, 0x808080);
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
                    context.drawTexture(SERVER_SELECTION_TEXTURE, x, y, 0, 32, 32, 32, 256, 256);
                } else {
                    context.drawTexture(SERVER_SELECTION_TEXTURE, x, y, 0, 0, 32, 32, 256, 256);
                }
            }
            if (index > 0) {
                if (o < 16 && p < 16) {
                    context.drawTexture(SERVER_SELECTION_TEXTURE, x, y, 96, 32, 32, 32, 256, 256);
                } else {
                    context.drawTexture(SERVER_SELECTION_TEXTURE, x, y, 96, 0, 32, 32, 256, 256);
                }
            }
            if (index < listSize - 1) {
                if (o < 16 && p > 16) {
                    context.drawTexture(SERVER_SELECTION_TEXTURE, x, y, 64, 32, 32, 32, 256, 256);
                } else {
                    context.drawTexture(SERVER_SELECTION_TEXTURE, x, y, 64, 0, 32, 32, 256, 256);
                }
            }
        }
    }
}
