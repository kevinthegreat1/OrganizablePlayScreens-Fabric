package com.kevinthegreat.organizableplayscreens.option;

import com.google.gson.*;
import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.gui.screen.OrganizablePlayScreensOptionsScreen;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;

import java.io.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;

public class OrganizablePlayScreensOptions {
    private static final Consumer<Integer> EMPTY_INT_CONSUMER = value -> {
    };
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Text X = Text.translatable("organizableplayscreens:options.x");
    public static final Text Y = Text.translatable("organizableplayscreens:options.y");
    public static final String[] KEYS = new String[]{"organizableplayscreens:options.backButton", "organizableplayscreens:options.moveEntryBackButton", "organizableplayscreens:options.newFolderButton", "organizableplayscreens:options.optionsButton", "organizableplayscreens:options.moveEntryIntoButton"};
    private final File optionsFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), OrganizablePlayScreens.MOD_ID + ".json");
    public final SimpleOption<Boolean> buttonType = new SimpleOption<>("organizableplayscreens:options.buttonType", SimpleOption.emptyTooltip(), (optionText, value) -> Text.translatable(value ? "organizableplayscreens:options.textField" : "organizableplayscreens:options.slider"), SimpleOption.BOOLEAN, false, value -> {
        if (MinecraftClient.getInstance().currentScreen instanceof OrganizablePlayScreensOptionsScreen organizablePlayScreensOptionsScreen) {
            organizablePlayScreensOptionsScreen.clearAndInit();
        }
    });
    public final SimpleOption<Integer> backButtonX = new SimpleOption<>(KEYS[0] + ".x", SimpleOption.emptyTooltip(), ScreenRelativeCallbacks.LEFT.valueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.LEFT, buttonType), 8, EMPTY_INT_CONSUMER);
    public final SimpleOption<Integer> backButtonY = new SimpleOption<>(KEYS[0] + ".y", SimpleOption.emptyTooltip(), ScreenRelativeCallbacks.TOP.valueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.TOP, buttonType), 8, EMPTY_INT_CONSUMER);
    public final SimpleOption<Integer> moveEntryBackButtonX = new SimpleOption<>(KEYS[1] + ".x", SimpleOption.emptyTooltip(), ScreenRelativeCallbacks.LEFT.valueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.LEFT, buttonType), 36, EMPTY_INT_CONSUMER);
    public final SimpleOption<Integer> moveEntryBackButtonY = new SimpleOption<>(KEYS[1] + ".y", SimpleOption.emptyTooltip(), ScreenRelativeCallbacks.TOP.valueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.TOP, buttonType), 8, EMPTY_INT_CONSUMER);
    public final SimpleOption<Integer> newFolderButtonX = new SimpleOption<>(KEYS[2] + ".x", SimpleOption.emptyTooltip(), ScreenRelativeCallbacks.RIGHT.valueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.RIGHT, buttonType), -56, EMPTY_INT_CONSUMER);
    public final SimpleOption<Integer> newFolderButtonY = new SimpleOption<>(KEYS[2] + ".y", SimpleOption.emptyTooltip(), ScreenRelativeCallbacks.TOP.valueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.TOP, buttonType), 8, EMPTY_INT_CONSUMER);
    public final SimpleOption<Integer> optionsButtonX = new SimpleOption<>(KEYS[3] + ".x", SimpleOption.emptyTooltip(), ScreenRelativeCallbacks.RIGHT.valueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.RIGHT, buttonType), -28, EMPTY_INT_CONSUMER);
    public final SimpleOption<Integer> optionsButtonY = new SimpleOption<>(KEYS[3] + ".y", SimpleOption.emptyTooltip(), ScreenRelativeCallbacks.TOP.valueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.TOP, buttonType), 8, EMPTY_INT_CONSUMER);
    public final SimpleOption<Integer> moveEntryIntoButtonX = new SimpleOption<>(KEYS[4] + ".x", SimpleOption.emptyTooltip(), ScreenRelativeCallbacks.RIGHT_LIST_WIDGET.valueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.RIGHT_LIST_WIDGET, buttonType), -30, EMPTY_INT_CONSUMER);
    public final SimpleOption<Integer> moveEntryIntoButtonY = new SimpleOption<>(KEYS[4] + ".y", SimpleOption.emptyTooltip(), ScreenRelativeCallbacks.TOP.valueTextGetter, new BothSuppliableIntSliderCallbacks(() -> 0, () -> 12, Integer::parseInt, String::valueOf, buttonType), 6, EMPTY_INT_CONSUMER);
    @SuppressWarnings("SuspiciousNameCombination")
    public final List<List<Pair<String, SimpleOption<?>>>> optionsArray = List.of(List.of(new Pair<>("backButton_x", backButtonX), new Pair<>("backButton_y", backButtonY)), List.of(new Pair<>("moveEntryBackButton_x", moveEntryBackButtonX), new Pair<>("moveEntryBackButton_y", moveEntryBackButtonY)), List.of(new Pair<>("newFolderButton_x", newFolderButtonX), new Pair<>("newFolderButton_y", newFolderButtonY)), List.of(new Pair<>("optionsButton_x", optionsButtonX), new Pair<>("optionsButton_y", optionsButtonY)), List.of(new Pair<>("moveEntryIntoButton_x", moveEntryIntoButtonX), new Pair<>("moveEntryIntoButton_y", moveEntryIntoButtonY)), List.of(new Pair<>("buttonType", buttonType)));

    public OrganizablePlayScreensOptions() {
        load();
    }

    private static int fromRightRelative(int value) {
        return MinecraftClient.getInstance().currentScreen == null ? 0 : MinecraftClient.getInstance().currentScreen.width + value;
    }

    private static int fromRightRelativeListWidget(int value) {
        return getListWidgetRight() + value;
    }

    private static int getListWidgetRight() {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof OrganizablePlayScreensOptionsScreen optionsScreen) {
            screen = optionsScreen.getParent();
        }
        if (screen instanceof MultiplayerScreen multiplayerScreen) {
            return multiplayerScreen.serverListWidget.getRowRight();
        }
        if (screen instanceof SelectWorldScreen selectWorldScreen) {
            return selectWorldScreen.levelList.getRowRight();
        }
        return screen == null ? Integer.MAX_VALUE - 1 : screen.width * 5 / 6;
    }

    public void load() {
        if (!optionsFile.exists()) {
            return;
        }
        JsonObject optionsJson;
        try (BufferedReader reader = new BufferedReader(new FileReader(optionsFile))) {
            optionsJson = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (FileNotFoundException e) {
            OrganizablePlayScreens.LOGGER.warn("Options file not found", e);
            return;
        } catch (IOException e) {
            OrganizablePlayScreens.LOGGER.error("Failed to load options", e);
            return;
        }
        for (List<Pair<String, SimpleOption<?>>> optionRow : optionsArray) {
            for (Pair<String, SimpleOption<?>> namedOption : optionRow) {
                parseOption(optionsJson, namedOption.getLeft(), namedOption.getRight());
            }
        }
    }

    private <T> void parseOption(JsonObject optionsJson, String name, SimpleOption<T> option) {
        DataResult<T> dataResult = option.getCodec().parse(JsonOps.INSTANCE, optionsJson.get(name));
        dataResult.error().ifPresent(error -> OrganizablePlayScreens.LOGGER.error("Error parsing option value " + optionsJson.get(name) + " for option " + name + ": " + error));
        dataResult.result().ifPresent(option::setValue);
    }

    public void save() {
        JsonObject optionsJson = new JsonObject();
        for (List<Pair<String, SimpleOption<?>>> optionRow : optionsArray) {
            for (Pair<String, SimpleOption<?>> namedOption : optionRow) {
                saveOption(optionsJson, namedOption.getLeft(), namedOption.getRight());
            }
        }
        File tempFile;
        try {
            tempFile = File.createTempFile(OrganizablePlayScreens.MOD_ID, ".json", optionsFile.getParentFile());
        } catch (IOException e) {
            OrganizablePlayScreens.LOGGER.error("Failed to save options file", e);
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            GSON.toJson(optionsJson, writer);
        } catch (IOException e) {
            OrganizablePlayScreens.LOGGER.error("Failed to write options", e);
        }
        File backup = new File(optionsFile.getParentFile(), OrganizablePlayScreens.MOD_ID + ".json_old");
        Util.backupAndReplace(optionsFile, tempFile, backup);
    }

    private <T> void saveOption(JsonObject optionsJson, String name, SimpleOption<T> option) {
        DataResult<JsonElement> dataResult = option.getCodec().encodeStart(JsonOps.INSTANCE, option.getValue());
        dataResult.error().ifPresent(error -> OrganizablePlayScreens.LOGGER.error("Error encoding option value " + option.getValue() + " for option " + name + ": " + error));
        dataResult.result().ifPresent(optionJson -> optionsJson.add(name, optionJson));
    }

    public int getNewFolderButtonX() {
        return fromRightRelative(newFolderButtonX.getValue());
    }

    public int getOptionsButtonX() {
        return fromRightRelative(optionsButtonX.getValue());
    }

    public int getMoveEntryIntoButtonX() {
        return fromRightRelativeListWidget(moveEntryIntoButtonX.getValue());
    }

    public void reset(List<Pair<String, SimpleOption<?>>> options) {
        for (Pair<String, SimpleOption<?>> option : options) {
            reset(option.getRight());
        }
    }

    public <T> void reset(SimpleOption<T> option) {
        option.setValue(option.defaultValue);
    }

    public enum ScreenRelativeCallbacks {
        LEFT(() -> 0, () -> MinecraftClient.getInstance().currentScreen == null ? Integer.MAX_VALUE - 1 : MinecraftClient.getInstance().currentScreen.width - 20, Integer::parseInt, String::valueOf, (optionText, value) -> GameOptions.getGenericValueText(X, value)),
        RIGHT(() -> MinecraftClient.getInstance().currentScreen == null ? Integer.MIN_VALUE : -MinecraftClient.getInstance().currentScreen.width, () -> -20, string -> MinecraftClient.getInstance().currentScreen == null ? 0 : Integer.parseInt(string) - MinecraftClient.getInstance().currentScreen.width, value -> String.valueOf(fromRightRelative(value)), (optionText, value) -> GameOptions.getGenericValueText(X, fromRightRelative(value))),
        RIGHT_LIST_WIDGET(() -> -getListWidgetRight(), () -> MinecraftClient.getInstance().currentScreen == null ? Integer.MAX_VALUE - 1 : MinecraftClient.getInstance().currentScreen.width - getListWidgetRight() - 20, string -> MinecraftClient.getInstance().currentScreen == null ? 0 : Integer.parseInt(string) - getListWidgetRight(), value -> String.valueOf(fromRightRelativeListWidget(value)), (optionText, value) -> GameOptions.getGenericValueText(X, fromRightRelativeListWidget(value))),
        TOP(() -> 0, () -> MinecraftClient.getInstance().currentScreen == null ? Integer.MAX_VALUE - 1 : MinecraftClient.getInstance().currentScreen.height - 20, Integer::parseInt, String::valueOf, (optionText, value) -> GameOptions.getGenericValueText(Y, value));
        public final IntSupplier minSupplier;
        public final IntSupplier maxSupplier;
        public final Function<String, Integer> valueParser;
        public final Function<Integer, String> valueGetter;
        public final SimpleOption.ValueTextGetter<Integer> valueTextGetter;

        ScreenRelativeCallbacks(IntSupplier minSupplier, IntSupplier maxSupplier, Function<String, Integer> valueParser, Function<Integer, String> valueGetter, SimpleOption.ValueTextGetter<Integer> valueTextGetter) {
            this.minSupplier = minSupplier;
            this.maxSupplier = maxSupplier;
            this.valueParser = valueParser;
            this.valueGetter = valueGetter;
            this.valueTextGetter = valueTextGetter;
        }
    }
}

