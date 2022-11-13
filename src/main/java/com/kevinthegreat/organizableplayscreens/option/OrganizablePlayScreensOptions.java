package com.kevinthegreat.organizableplayscreens.option;

import com.kevinthegreat.organizableplayscreens.gui.screen.OrganizablePlayScreensOptionsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class OrganizablePlayScreensOptions {
    private static final Consumer<Integer> EMPTY_CONSUMER = value -> {
    };
    private static final Text X = Text.translatable("organizableplayscreens:options.x");
    private static final Text Y = Text.translatable("organizableplayscreens:options.y");
    public static final String[] KEYS = new String[]{"organizableplayscreens:options.backButton", "organizableplayscreens:options.moveEntryBackButton", "organizableplayscreens:options.newFolderButton", "organizableplayscreens:options.optionsButton", "organizableplayscreens:options.moveEntryIntoButton"};
    private static final SimpleOption.ValueTextGetter<Integer> LEFT_RELATIVE_TEXT_GETTER = (optionText, value) -> GameOptions.getGenericValueText(X, value);
    private static final SimpleOption.ValueTextGetter<Integer> RIGHT_RELATIVE_TEXT_GETTER = (optionText, value) -> GameOptions.getGenericValueText(X, fromRightRelative(value));
    private static final SimpleOption.ValueTextGetter<Integer> RIGHT_RELATIVE_LIST_WIDGET_TEXT_GETTER = (optionText, value) -> GameOptions.getGenericValueText(X, fromRightRelativeListWidget(value));
    private static final SimpleOption.ValueTextGetter<Integer> TOP_RELATIVE_TEXT_GETTER = (optionText, value) -> GameOptions.getGenericValueText(Y, value);
    public final SimpleOption<Integer> backButtonX = new SimpleOption<>(KEYS[0] + ".x", SimpleOption.emptyTooltip(), LEFT_RELATIVE_TEXT_GETTER, createLeftRelativeWidthCallbacks(), 8, EMPTY_CONSUMER);
    public final SimpleOption<Integer> backButtonY = new SimpleOption<>(KEYS[0] + ".y", SimpleOption.emptyTooltip(), TOP_RELATIVE_TEXT_GETTER, createTopRelativeHeightCallbacks(), 8, EMPTY_CONSUMER);
    public final SimpleOption<Integer> moveEntryBackButtonX = new SimpleOption<>(KEYS[1] + ".x", SimpleOption.emptyTooltip(), LEFT_RELATIVE_TEXT_GETTER, createLeftRelativeWidthCallbacks(), 36, EMPTY_CONSUMER);
    public final SimpleOption<Integer> moveEntryBackButtonY = new SimpleOption<>(KEYS[1] + ".y", SimpleOption.emptyTooltip(), TOP_RELATIVE_TEXT_GETTER, createTopRelativeHeightCallbacks(), 8, EMPTY_CONSUMER);
    public final SimpleOption<Integer> newFolderButtonX = new SimpleOption<>(KEYS[2] + ".x", SimpleOption.emptyTooltip(), RIGHT_RELATIVE_TEXT_GETTER, createRightRelativeWidthCallbacks(), -56, EMPTY_CONSUMER);
    public final SimpleOption<Integer> newFolderButtonY = new SimpleOption<>(KEYS[2] + ".y", SimpleOption.emptyTooltip(), TOP_RELATIVE_TEXT_GETTER, createTopRelativeHeightCallbacks(), 8, EMPTY_CONSUMER);
    public final SimpleOption<Integer> optionsButtonX = new SimpleOption<>(KEYS[3] + ".x", SimpleOption.emptyTooltip(), RIGHT_RELATIVE_TEXT_GETTER, createRightRelativeWidthCallbacks(), -28, EMPTY_CONSUMER);
    public final SimpleOption<Integer> optionsButtonY = new SimpleOption<>(KEYS[3] + ".y", SimpleOption.emptyTooltip(), TOP_RELATIVE_TEXT_GETTER, createTopRelativeHeightCallbacks(), 8, EMPTY_CONSUMER);
    public final SimpleOption<Integer> moveEntryIntoButtonX = new SimpleOption<>(KEYS[4] + ".x", SimpleOption.emptyTooltip(), RIGHT_RELATIVE_LIST_WIDGET_TEXT_GETTER, createRightRelativeListWidgetCallbacks(), -30, EMPTY_CONSUMER);
    public final SimpleOption<Integer> moveEntryIntoButtonY = new SimpleOption<>(KEYS[4] + ".y", SimpleOption.emptyTooltip(), TOP_RELATIVE_TEXT_GETTER, new SimpleOption.ValidatingIntSliderCallbacks(0, 12), 6, EMPTY_CONSUMER);
    public final SimpleOption<?>[][] optionsArray = new SimpleOption[][]{new SimpleOption[]{backButtonX, backButtonY}, new SimpleOption[]{moveEntryBackButtonX, moveEntryBackButtonY}, new SimpleOption[]{newFolderButtonX, newFolderButtonY}, new SimpleOption[]{optionsButtonX, optionsButtonY}, new SimpleOption[]{moveEntryIntoButtonX, moveEntryIntoButtonY}};

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
        return screen == null ? 0 : screen.width * 5 / 6;
    }

    private MaxSuppliableIntSliderCallbacks createLeftRelativeWidthCallbacks() {
        return new MaxSuppliableIntSliderCallbacks(0, () -> MinecraftClient.getInstance().currentScreen == null ? 0x7FFFFFFE : MinecraftClient.getInstance().currentScreen.width - 20);
    }

    private MinSuppliableIntSliderCallbacks createRightRelativeWidthCallbacks() {
        return new MinSuppliableIntSliderCallbacks(() -> MinecraftClient.getInstance().currentScreen == null ? 0x7FFFFFFE : -MinecraftClient.getInstance().currentScreen.width, -20);
    }

    private BothSuppliableIntSliderCallbacks createRightRelativeListWidgetCallbacks() {
        return new BothSuppliableIntSliderCallbacks(() -> -getListWidgetRight(), () -> MinecraftClient.getInstance().currentScreen == null ? 0x7FFFFFFE : MinecraftClient.getInstance().currentScreen.width - getListWidgetRight() - 20);
    }

    private MaxSuppliableIntSliderCallbacks createTopRelativeHeightCallbacks() {
        return new MaxSuppliableIntSliderCallbacks(0, () -> MinecraftClient.getInstance().currentScreen == null ? 0x7FFFFFFE : MinecraftClient.getInstance().currentScreen.height - 20);
    }

    public SimpleOption<?>[][] asArray() {
        return optionsArray;
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

    public void reset(SimpleOption<?>[] options) {
        for (SimpleOption<?> option : options) {
            reset(option);
        }
    }

    public <T> void reset(SimpleOption<T> option) {
        option.setValue(option.defaultValue);
    }
}

