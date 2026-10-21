package com.kevinthegreat.organizableplayscreens.option;

import com.google.gson.*;
import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.gui.screen.OrganizablePlayScreensButtonDragScreen;
import com.kevinthegreat.organizableplayscreens.gui.screen.OrganizablePlayScreensButtonOptionsScreen;
import com.kevinthegreat.organizableplayscreens.gui.screen.OrganizablePlayScreensOptionsScreen;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.JoinMultiplayerScreenAccessor;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.OptionInstanceAccessor;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.SelectWorldScreenAccessor;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;

/**
 * Holds all options for Organizable Play Screens.
 * The options are serialized and deserialized with Codec and Gson and saved in {@code /config/organizableplayscreens.json}.
 */
public class OrganizablePlayScreensOptions {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Component X = Component.translatable("organizableplayscreens:options.x");
    public static final Component Y = Component.translatable("organizableplayscreens:options.y");
    public static final String[] KEYS = new String[]{"organizableplayscreens:options.backButton", "organizableplayscreens:options.moveEntryBackButton", "organizableplayscreens:options.newFolderButton", "organizableplayscreens:options.optionsButton", "organizableplayscreens:options.moveEntryIntoButton"};
    private final Path optionsFile = FabricLoader.getInstance().getConfigDir().resolve(OrganizablePlayScreens.MOD_ID + ".json");
    public final OptionInstance<Boolean> buttonType = new OptionInstance<>("organizableplayscreens:options.buttonType", OptionInstance.noTooltip(), (_, value) -> Component.translatable(value ? "organizableplayscreens:options.textField" : "organizableplayscreens:options.slider"), OptionInstance.BOOLEAN_VALUES, false, _ -> {
        if (Minecraft.getInstance().gui.screen() instanceof OrganizablePlayScreensButtonOptionsScreen organizablePlayScreensButtonOptionsScreen) {
            Minecraft.getInstance().gui.setScreen(new OrganizablePlayScreensButtonOptionsScreen(organizablePlayScreensButtonOptionsScreen.getParent()));
        }
    });
    public final OptionInstance<Integer> backButtonX = new OptionInstance<>(KEYS[0] + ".x", OptionInstance.noTooltip(), ScreenRelativeCallbacks.LEFT.displayValueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.LEFT, buttonType), 8, _ -> updateResetButton(0));
    public final OptionInstance<Integer> backButtonY = new OptionInstance<>(KEYS[0] + ".y", OptionInstance.noTooltip(), ScreenRelativeCallbacks.TOP.displayValueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.TOP, buttonType), 8, _ -> updateResetButton(0));
    public final OptionInstance<Integer> moveEntryBackButtonX = new OptionInstance<>(KEYS[1] + ".x", OptionInstance.noTooltip(), ScreenRelativeCallbacks.LEFT.displayValueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.LEFT, buttonType), 36, _ -> updateResetButton(1));
    public final OptionInstance<Integer> moveEntryBackButtonY = new OptionInstance<>(KEYS[1] + ".y", OptionInstance.noTooltip(), ScreenRelativeCallbacks.TOP.displayValueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.TOP, buttonType), 8, _ -> updateResetButton(1));
    public final OptionInstance<Integer> newFolderButtonX = new OptionInstance<>(KEYS[2] + ".x", OptionInstance.noTooltip(), ScreenRelativeCallbacks.RIGHT.displayValueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.RIGHT, buttonType), -56, _ -> updateResetButton(2));
    public final OptionInstance<Integer> newFolderButtonY = new OptionInstance<>(KEYS[2] + ".y", OptionInstance.noTooltip(), ScreenRelativeCallbacks.TOP.displayValueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.TOP, buttonType), 8, _ -> updateResetButton(2));
    public final OptionInstance<Integer> optionsButtonX = new OptionInstance<>(KEYS[3] + ".x", OptionInstance.noTooltip(), ScreenRelativeCallbacks.RIGHT.displayValueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.RIGHT, buttonType), -28, _ -> updateResetButton(3));
    public final OptionInstance<Integer> optionsButtonY = new OptionInstance<>(KEYS[3] + ".y", OptionInstance.noTooltip(), ScreenRelativeCallbacks.TOP.displayValueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.TOP, buttonType), 8, _ -> updateResetButton(3));
    public final OptionInstance<Integer> moveEntryIntoButtonX = new OptionInstance<>(KEYS[4] + ".x", OptionInstance.noTooltip(), ScreenRelativeCallbacks.RIGHT_LIST_WIDGET.displayValueTextGetter, new BothSuppliableIntSliderCallbacks(ScreenRelativeCallbacks.RIGHT_LIST_WIDGET, buttonType), -30, _ -> updateResetButton(4));
    public final OptionInstance<Integer> moveEntryIntoButtonY = new OptionInstance<>(KEYS[4] + ".y", OptionInstance.noTooltip(), ScreenRelativeCallbacks.TOP.displayValueTextGetter, new BothSuppliableIntSliderCallbacks(0, 12, buttonType), 6, _ -> updateResetButton(4));
    public final OptionInstance<Boolean> showBackButton = new OptionInstance<>("organizableplayscreens:options.showBackButton", OptionInstance.noTooltip(), (_, value) -> Component.translatable(value ? "organizableplayscreens:options.shown" : "organizableplayscreens:options.hidden"), OptionInstance.BOOLEAN_VALUES, true, _ -> save());
    public final OptionInstance<Boolean> showMoveEntryBackButton = new OptionInstance<>("organizableplayscreens:options.showMoveEntryBackButton", OptionInstance.noTooltip(), (_, value) -> Component.translatable(value ? "organizableplayscreens:options.shown" : "organizableplayscreens:options.hidden"), OptionInstance.BOOLEAN_VALUES, true, _ -> save());
    public final OptionInstance<Boolean> showNewFolderButton = new OptionInstance<>("organizableplayscreens:options.showNewFolderButton", OptionInstance.noTooltip(), (_, value) -> Component.translatable(value ? "organizableplayscreens:options.shown" : "organizableplayscreens:options.hidden"), OptionInstance.BOOLEAN_VALUES, true, _ -> save());
    public final OptionInstance<Boolean> showOptionsButton = new OptionInstance<>("organizableplayscreens:options.showOptionsButton", OptionInstance.noTooltip(), (_, value) -> Component.translatable(value ? "organizableplayscreens:options.shown" : "organizableplayscreens:options.hidden"), OptionInstance.BOOLEAN_VALUES, true, _ -> save());
    public final OptionInstance<Boolean> showMoveEntryIntoButton = new OptionInstance<>("organizableplayscreens:options.showMoveEntryIntoButton", OptionInstance.noTooltip(), (_, value) -> Component.translatable(value ? "organizableplayscreens:options.shown" : "organizableplayscreens:options.hidden"), OptionInstance.BOOLEAN_VALUES, true, _ -> save());
    public final List<OptionInstance<Boolean>> showButtonOptions = Arrays.asList(showBackButton, showMoveEntryBackButton, showNewFolderButton, showOptionsButton, showMoveEntryIntoButton);
    public final List<List<Pair<String, OptionInstance<?>>>> optionsArray = List.of(List.of(Pair.of("backButton_x", backButtonX), Pair.of("backButton_y", backButtonY)), List.of(Pair.of("moveEntryBackButton_x", moveEntryBackButtonX), Pair.of("moveEntryBackButton_y", moveEntryBackButtonY)), List.of(Pair.of("newFolderButton_x", newFolderButtonX), Pair.of("newFolderButton_y", newFolderButtonY)), List.of(Pair.of("optionsButton_x", optionsButtonX), Pair.of("optionsButton_y", optionsButtonY)), List.of(Pair.of("moveEntryIntoButton_x", moveEntryIntoButtonX), Pair.of("moveEntryIntoButton_y", moveEntryIntoButtonY)), List.of(Pair.of("buttonType", buttonType)), List.of(Pair.of("showBackButton", showBackButton)), List.of(Pair.of("showMoveEntryBackButton", showMoveEntryBackButton)), List.of(Pair.of("showNewFolderButton", showNewFolderButton)), List.of(Pair.of("showOptionsButton", showOptionsButton)), List.of(Pair.of("showMoveEntryIntoButton", showMoveEntryIntoButton)));

    public OrganizablePlayScreensOptions() {
        load();
    }

    @SuppressWarnings("ConstantValue")
    private static int hasScreen(ToIntFunction<Screen> function, int fallback) {
        return Minecraft.getInstance().gui != null && Minecraft.getInstance().gui.screen() != null ? function.applyAsInt(Minecraft.getInstance().gui.screen()) : fallback;
    }

    /**
     * Calculates display value for a right relative value.
     *
     * @param value the right relative value
     * @return the display value
     * @see ScreenRelativeCallbacks#RIGHT
     */
    private static int fromRightRelative(int value) {
        return hasScreen(screen -> screen.width + value, 0);
    }

    /**
     * Calculates display value for a right list widget relative value.
     *
     * @param value the right list widget relative value
     * @return the display value
     * @see ScreenRelativeCallbacks#RIGHT_LIST_WIDGET
     */
    private static int fromRightRelativeListWidget(int value) {
        return getListWidgetRight() + value;
    }

    /**
     * Gets the right of the list widget.
     *
     * @return the right of the list widget, or {@code 5/6} of the screen width if the list widget could not be found
     * @see #fromRightRelativeListWidget(int)
     */
    private static int getListWidgetRight() {
        @SuppressWarnings("ConstantValue")
        Screen screen = Minecraft.getInstance().gui != null ? Minecraft.getInstance().gui.screen() : null;
        while (screen != null) {
            if (screen instanceof OrganizablePlayScreensOptionsScreen optionsScreen) {
                screen = optionsScreen.getParent();
            } else if (screen instanceof OrganizablePlayScreensButtonOptionsScreen optionsScreen) {
                screen = optionsScreen.getParent();
            } else if (screen instanceof OrganizablePlayScreensButtonDragScreen optionsScreen) {
                screen = optionsScreen.getParent();
            } else {
                break;
            }
        }
        if (screen instanceof JoinMultiplayerScreen multiplayerScreen) {
            return ((JoinMultiplayerScreenAccessor) multiplayerScreen).getServerSelectionList().getRowRight();
        }
        if (screen instanceof SelectWorldScreen selectWorldScreen) {
            return ((SelectWorldScreenAccessor) selectWorldScreen).getList().getRowRight();
        }
        return screen == null ? Integer.MAX_VALUE - 1 : screen.width * 5 / 6;
    }

    /**
     * Loads options from {@code /config/organizable-play-screens.json} with Gson.
     */
    public void load() {
        if (!Files.isRegularFile(optionsFile)) {
            return;
        }
        JsonObject optionsJson;
        try (BufferedReader reader = Files.newBufferedReader(optionsFile)) {
            optionsJson = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (FileNotFoundException e) {
            OrganizablePlayScreens.LOGGER.warn("Options file not found", e);
            return;
        } catch (IOException e) {
            OrganizablePlayScreens.LOGGER.error("Failed to load options", e);
            return;
        }
        for (List<Pair<String, OptionInstance<?>>> optionRow : optionsArray) {
            for (Pair<String, OptionInstance<?>> namedOption : optionRow) {
                parseOption(optionsJson, namedOption.key(), namedOption.value());
            }
        }
    }

    /**
     * Parses an option from a {@link JsonObject} with Codec.
     *
     * @param optionsJson the {@link JsonObject} to parse from
     * @param name        the name of the option
     * @param option      the option to parse to
     */
    private <T> void parseOption(JsonObject optionsJson, String name, OptionInstance<T> option) {
        DataResult<T> dataResult = option.codec().parse(JsonOps.INSTANCE, optionsJson.get(name));
        dataResult.ifError(error -> OrganizablePlayScreens.LOGGER.error("Error parsing option value {} for option {}: {}", optionsJson.get(name), name, error));
        dataResult.ifSuccess(option::set);
    }

    /**
     * Saves options to {@code /config/organizable-play-screens.json} with Gson.
     */
    public void save() {
        JsonObject optionsJson = new JsonObject();
        for (List<Pair<String, OptionInstance<?>>> optionRow : optionsArray) {
            for (Pair<String, OptionInstance<?>> namedOption : optionRow) {
                saveOption(optionsJson, namedOption.key(), namedOption.value());
            }
        }
        Path tempFile;
        try {
            tempFile = Files.createTempFile(optionsFile.getParent(), OrganizablePlayScreens.MOD_ID, ".json");
        } catch (IOException e) {
            OrganizablePlayScreens.LOGGER.error("Failed to save options file", e);
            return;
        }
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
            GSON.toJson(optionsJson, writer);
        } catch (IOException e) {
            OrganizablePlayScreens.LOGGER.error("Failed to write options", e);
        }
        Path backup = optionsFile.getParent().resolve(OrganizablePlayScreens.MOD_ID + ".json_old");
        Util.safeReplaceFile(optionsFile, tempFile, backup);
    }

    /**
     * Saves an option to a {@link JsonObject} with Codec.
     *
     * @param optionsJson the {@link JsonObject} to save to
     * @param name        the name of the option
     * @param option      the option to save
     */
    private <T> void saveOption(JsonObject optionsJson, String name, OptionInstance<T> option) {
        DataResult<JsonElement> dataResult = option.codec().encodeStart(JsonOps.INSTANCE, option.get());
        dataResult.ifError(error -> OrganizablePlayScreens.LOGGER.error("Error encoding option value {} for option {}: {}", option.get(), name, error));
        dataResult.ifSuccess(optionJson -> optionsJson.add(name, optionJson));
    }

    /**
     * Gets the value for an integer {@link OptionInstance}.
     *
     * @param option the option to get the value from
     * @return the value
     */
    public int getValue(OptionInstance<Integer> option) {
        return option.values() instanceof BothSuppliableIntSliderCallbacks suppliableCallbacks ? suppliableCallbacks.displayValueGetter().apply(option.get()) : option.get();
    }

    /**
     * Updates all the reset buttons.
     *
     * @see #updateResetButton(int)
     */
    public void updateResetButtons() {
        for (int i = 0; i < 5; i++) {
            updateResetButton(i);
        }
    }

    /**
     * Updates the reset button for this option.
     * Activates the reset button if any option in the row is not its default value.
     * Deactivates the reset button if all options are their default values.
     *
     * @param row the row to update
     */
    public void updateResetButton(int row) {
        if (Minecraft.getInstance().gui.screen() instanceof OrganizablePlayScreensButtonOptionsScreen optionsScreen) {
            optionsScreen.updateResetButton(row, optionsArray.get(row).stream().map(Pair::value).anyMatch(OrganizablePlayScreensOptions::notDefault));
        }
    }

    /**
     * Checks if an option is not its default value.
     *
     * @param option the option to check
     * @return true if the option is not its default value
     */
    public static <T> boolean notDefault(OptionInstance<T> option) {
        return option.get() != ((OptionInstanceAccessor) (Object) option).getInitialValue();
    }

    /**
     * Resets a list of options to their default values.
     *
     * @param options the options to reset
     */
    public static void reset(List<Pair<String, OptionInstance<?>>> options) {
        for (Pair<String, OptionInstance<?>> option : options) {
            reset(option.value());
        }
    }

    /**
     * Resets an option to its default value.
     *
     * @param option the option to reset
     */
    public static <T> void reset(OptionInstance<T> option) {
        option.set(((OptionInstanceAccessor) (Object) option).getInitialValue());
    }

    /**
     * A helper enum for creating {@link BothSuppliableIntSliderCallbacks} for options regarding button locations in screens.
     * Locations are stored as relative to different parts of the screen to make different screen sizes work.
     * Buttons need to be 20 by 20 pixels for this to bound values correctly.
     */
    public enum ScreenRelativeCallbacks {
        /**
         * This will store option values relative to the left side of a screen.
         */
        LEFT(0, () -> hasScreen(screen -> screen.width - 20, Integer.MAX_VALUE - 1), X),
        /**
         * This will store option values relative to the right side of a screen.
         */
        RIGHT(() -> hasScreen(screen -> -screen.width, Integer.MIN_VALUE), -20, string -> hasScreen(screen -> Integer.parseInt(string) - screen.width, 0), OrganizablePlayScreensOptions::fromRightRelative, (_, value) -> Options.genericValueLabel(X, value)),
        /**
         * This will store option values relative to the right side of the list widget in a screen.
         * The right side of list widget will be assumed to be {@code 5/6} of the screen width if a {@link JoinMultiplayerScreen} or a {@link SelectWorldScreen} could not be found.
         */
        RIGHT_LIST_WIDGET(() -> -getListWidgetRight(), () -> hasScreen(screen -> screen.width - getListWidgetRight() - 20, Integer.MAX_VALUE - 1), string -> hasScreen(_ -> Integer.parseInt(string) - getListWidgetRight(), 0), OrganizablePlayScreensOptions::fromRightRelativeListWidget, (_, value) -> Options.genericValueLabel(X, value)),
        /**
         * This will store option values relative to the top of a screen.
         */
        TOP(0, () -> hasScreen(screen -> screen.height - 20, Integer.MAX_VALUE - 1), Y);
        /**
         * Supplier for the minimum value of the option.
         */
        public final IntSupplier minSupplier;
        /**
         * Supplier for the maximum value of the option.
         */
        public final IntSupplier maxSupplier;
        /**
         * Function to parse option value from display value string.
         */
        public final Function<String, Integer> displayValueParser;
        /**
         * Function to transform option value to display value.
         */
        public final UnaryOperator<Integer> displayValueGetter;
        /**
         * Getter for the display text of an option value.
         */
        public final OptionInstance.CaptionBasedToString<Integer> displayValueTextGetter;

        ScreenRelativeCallbacks(int minInclusive, IntSupplier maxSupplier, Component displayTextPrefix) {
            this(() -> minInclusive, maxSupplier, Integer::parseInt, UnaryOperator.identity(), (_, value) -> Options.genericValueLabel(displayTextPrefix, value));
        }

        ScreenRelativeCallbacks(IntSupplier minSupplier, int maxInclusive, Function<String, Integer> displayValueParser, UnaryOperator<Integer> displayValueGetter, OptionInstance.CaptionBasedToString<Integer> valueTextGetter) {
            this(minSupplier, () -> maxInclusive, displayValueParser, displayValueGetter, valueTextGetter);
        }

        ScreenRelativeCallbacks(IntSupplier minSupplier, IntSupplier maxSupplier, Function<String, Integer> displayValueParser, UnaryOperator<Integer> displayValueGetter, OptionInstance.CaptionBasedToString<Integer> valueTextGetter) {
            this.minSupplier = minSupplier;
            this.maxSupplier = maxSupplier;
            this.displayValueParser = displayValueParser;
            this.displayValueGetter = displayValueGetter;
            this.displayValueTextGetter = (optionText, value) -> valueTextGetter.toString(optionText, displayValueGetter.apply(value));
        }
    }
}

