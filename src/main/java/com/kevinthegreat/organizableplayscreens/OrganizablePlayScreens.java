package com.kevinthegreat.organizableplayscreens;

import com.kevinthegreat.organizableplayscreens.gui.EntryType;
import com.kevinthegreat.organizableplayscreens.mixin.WorldListWidgetMixin;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

public class OrganizablePlayScreens implements ModInitializer {
    public static final String MOD_ID = "organizableplayscreens";
    public static final String MOD_NAME = "Organizable Play Screens";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Identifier SERVER_SELECTION_TEXTURE = new Identifier("textures/gui/server_selection.png");
    public static final Identifier OPTIONS_BUTTON_TEXTURE = new Identifier(MOD_ID, "textures/gui/options_button.png");
    public static final Tooltip MOVE_ENTRY_INTO_TOOLTIP = Tooltip.of(Text.translatable(MOD_ID + ":folder.moveInto"));
    public static final Tooltip MOVE_ENTRY_BACK_TOOLTIP = Tooltip.of(Text.translatable(MOD_ID + ":folder.moveBack"));
    private static OrganizablePlayScreens instance;
    public final OrganizablePlayScreensOptions options;

    public OrganizablePlayScreens() {
        instance = this;
        options = new OrganizablePlayScreensOptions();
    }

    public static OrganizablePlayScreens getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {
        LOGGER.info(MOD_NAME + " initialized.");
    }

    /**
     * Sorts a list of {@link WorldListWidget.WorldEntry} with {@link net.minecraft.world.level.storage.LevelSummary#compareTo(net.minecraft.world.level.storage.LevelSummary)}.
     *
     * @param worldEntries The list of {@link WorldListWidget.WorldEntry} to sort.
     */
    public static void sortWorldEntries(List<WorldListWidget.WorldEntry> worldEntries) {
        worldEntries.sort(Comparator.comparing(worldEntry -> ((WorldListWidgetMixin.WorldEntryAccessor) worldEntry).getLevel()));
    }

    /**
     * Renders a folder entry with the given parameters.
     *
     * @param name           The name of the folder.
     * @param listSize       The size of the entry list that the folder is in.
     * @param buttonMoveInto The button to move the selected entry into the folder.
     */
    public static void renderFolderEntry(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize, ButtonWidget buttonMoveInto) {
        context.drawTextWithShadow(client.textRenderer, name, x + 32 + 3, y + 1, 0xFFFFFF);
        context.drawTextWithShadow(client.textRenderer, EntryType.FOLDER.text(), x + 32 + 3, y + 12, 0x808080);
        renderEntry(context, index, y, x, mouseX, mouseY, hovered, listSize, true);
        OrganizablePlayScreensOptions options = OrganizablePlayScreens.getInstance().options;
        buttonMoveInto.setPosition(options.getValue(options.moveEntryIntoButtonX), y + options.moveEntryIntoButtonY.getValue());
        buttonMoveInto.render(context, mouseX, mouseY, tickDelta);
    }

    public static void renderSectionEntry(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, String name, int listSize) {
        context.drawTextWithShadow(client.textRenderer, name, x + 32 + 3, y + 1, 0xFFFFFF);
        context.drawTextWithShadow(client.textRenderer, EntryType.SECTION.text(), x + 32 + 3, y + 12, 0x808080);
        renderEntry(context, index, y, x, mouseX, mouseY, hovered, listSize, false);
    }

    public static void renderSeparatorEntry(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, String name, int listSize) {
        context.drawTextWithShadow(client.textRenderer, "--------------------------------------", x + 32 + 3, y + 12, 0xFFFFFF);
        context.drawTextWithShadow(client.textRenderer, name, x + 32 + 3, y + 23, 0x808080);
        renderEntry(context, index, y, x, mouseX, mouseY, hovered, listSize, false);
    }

    private static void renderEntry(DrawContext context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, int listSize, boolean renderOpenButton) {
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

    public static void updateEntryNbt(NbtCompound nbtEntry, boolean multiplayer) {
        if (!nbtEntry.contains("type", NbtElement.STRING_TYPE)) {
            nbtEntry.putString("type", nbtEntry.getBoolean("type") ? OrganizablePlayScreens.MOD_ID + ":folder" : multiplayer ? "minecraft:server" : "minecraft:world");
        }
    }
}
