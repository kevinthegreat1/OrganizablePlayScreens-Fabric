package com.kevinthegreat.organizableplayscreens.gui.screen;

import com.google.common.collect.ImmutableList;
import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.List;

/**
 * The options screen for Organizable Play Screens.
 */
public class OrganizablePlayScreensOptionsScreen extends GameOptionsScreen {
    /**
     * {@code 'X:'} text used in front of text field options.
     */
    private static final Text X_COLON = GameOptions.getGenericValueText(OrganizablePlayScreensOptions.X, Text.of(""));
    /**
     * {@code 'Y:'} text used in front of text field options.
     */
    private static final Text Y_COLON = GameOptions.getGenericValueText(OrganizablePlayScreensOptions.Y, Text.of(""));
    /**
     * The {@link OrganizablePlayScreensOptions} instance used for loading, storing, and saving the options.
     */
    private final OrganizablePlayScreensOptions options;
    /**
     * The reset buttons ordered by row.
     */
    private List<ButtonWidget> resetButtons;

    public OrganizablePlayScreensOptionsScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("organizableplayscreens:options.title"));
        options = OrganizablePlayScreens.getInstance().options;
    }

    /**
     * Gets the parent screen.
     * Public for {@link OrganizablePlayScreensOptions} to access potential
     * {@link net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen MultiplayerScreen} and
     * {@link net.minecraft.client.gui.screen.world.SelectWorldScreen SelectWorldScreen} instances.
     *
     * @return the parent screen
     */
    public Screen getParent() {
        return parent;
    }

    /**
     * Initializes the screen.
     * Creates option buttons and reset buttons row by row.
     * Adds the reset buttons to {@link #resetButtons}.
     * Creates button type button and done button at the end.
     */
    @Override
    protected void init() {
        super.init();
        int i = 0;
        ImmutableList.Builder<ButtonWidget> resetButtonsBuilder = ImmutableList.builderWithExpectedSize(5);
        for (List<Pair<String, SimpleOption<?>>> optionRow : options.optionsArray) {
            int j = 0;
            int y = height / 6 - 1 + i * 36;
            for (Pair<String, SimpleOption<?>> namedOption : optionRow) {
                int x = width / 2 - 155 + j * 135;
                addDrawableChild(namedOption.getRight().createWidget(gameOptions, x, y, 125));
                j++;
            }
            ButtonWidget resetButton = ButtonWidget.builder(Text.translatable("controls.reset"), (buttonWidget) -> {
                OrganizablePlayScreensOptions.reset(optionRow);
                clearAndInit();
            }).dimensions(width / 2 - 155 + j * 135, y, 40, 20).build();
            resetButtonsBuilder.add(resetButton);
            addDrawableChild(resetButton);
            if (i++ == 4) {
                break;
            }
        }
        resetButtons = resetButtonsBuilder.build();
        addDrawableChild(options.buttonType.createWidget(gameOptions, width / 2 - 155, height - 28, 150));
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (buttonWidget) -> {
            options.save();
            client.setScreen(parent);
        }).dimensions(width / 2 + 5, height - 28, 150, 20).build());
        options.updateResetButtons();
    }

    /**
     * Clears and re-initializes the screen.
     * Used to replace buttons when option value is reset or when button type is changed.
     * Overridden to make method public so {@link OrganizablePlayScreensOptions} can call this when changing button type.
     */
    @Override
    public void clearAndInit() {
        super.clearAndInit();
    }

    /**
     * Renders the screen.
     * Draws the title and option titles. Then, draws the {@code 'X:'} and {@code 'Y:'} text if text fields are being used.
     * Finally, draws the buttons with {@link Screen#render(DrawContext, int, int, float) super.render(MatrixStack, int, int, float)}.
     *
     * @param context the draw context
     * @param mouseX  the x position of the mouse
     * @param mouseY  the y position of the mouse
     * @param delta   the time between ticks
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFF);
        int i = 0;
        for (String key : OrganizablePlayScreensOptions.KEYS) {
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable(key), width / 2, height / 6 - 12 + i, 0xFFFFFF);
            i += 36;
        }
        if (options.buttonType.getValue()) {
            for (i = 0; i < 5; i++) {
                int y = height / 6 - 1 + i * 36;
                int x = width / 2 - 155;
                context.drawTextWithShadow(textRenderer, X_COLON, x + 5, y + 6, 0xFFFFFF);
                x = width / 2 - 155 + 135;
                context.drawTextWithShadow(textRenderer, Y_COLON, x + 5, y + 6, 0xFFFFFF);
            }
        }
        super.render(context, mouseX, mouseY, delta);
    }

    /**
     * Saves the options when closing the screen.
     */
    @Override
    public void removed() {
        options.save();
    }

    /**
     * Updates the reset button at the given index with the given state.
     *
     * @param resetButtonIndex the index of the reset button to update
     * @param active           whether the reset button should be active
     */
    public void updateResetButton(int resetButtonIndex, boolean active) {
        resetButtons.get(resetButtonIndex).active = active;
    }
}
