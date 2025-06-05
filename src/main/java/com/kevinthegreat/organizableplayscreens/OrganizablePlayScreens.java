package com.kevinthegreat.organizableplayscreens;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.mixin.WorldListWidgetMixin;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.nbt.NbtCompound;
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
    public static final Identifier OPTIONS_BUTTON_TEXTURE = Identifier.of(MOD_ID, "textures/gui/options_button.png");
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

    public static void updateEntryNbt(NbtCompound nbtEntry, boolean multiplayer) {
        if (nbtEntry.getString("type").isEmpty()) {
            nbtEntry.putString("type", nbtEntry.getBoolean("type", false) ? EntryType.FOLDER.id().toString() : multiplayer ? "minecraft:server" : "minecraft:world");
        }
    }
}
