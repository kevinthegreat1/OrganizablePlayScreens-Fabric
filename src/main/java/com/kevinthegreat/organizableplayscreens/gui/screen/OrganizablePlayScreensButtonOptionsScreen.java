package com.kevinthegreat.organizableplayscreens.gui.screen;

import com.google.common.collect.ImmutableList;
import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.List;

public class OrganizablePlayScreensButtonOptionsScreen extends GameOptionsScreen {
    private static final Identifier MENU_LIST_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_menu_list_background.png");
    private static final int MARGIN_TOP = 46;
    private static final int ROW_HEIGHT = 34;
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

    public OrganizablePlayScreensButtonOptionsScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("organizableplayscreens:options.buttonOptions"));
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
     * Initializes the body of the screen.
     * Creates option buttons and reset buttons row by row.
     * Adds the reset buttons to {@link #resetButtons}.
     */
    @Override
    protected void initBody() {
        int i = 0;
        ImmutableList.Builder<ButtonWidget> resetButtonsBuilder = ImmutableList.builderWithExpectedSize(5);
        for (List<Pair<String, SimpleOption<?>>> optionRow : options.optionsArray) {
            int j = 0;
            int y = MARGIN_TOP + i * ROW_HEIGHT;
            for (Pair<String, SimpleOption<?>> namedOption : optionRow) {
                int x = width / 2 - 155 + j * 135;
                addDrawableChild(namedOption.getRight().createWidget(gameOptions, x, y, 125));
                j++;
            }
            ButtonWidget resetButton = ButtonWidget.builder(Text.translatable("controls.reset"), (buttonWidget) -> {
                OrganizablePlayScreensOptions.reset(optionRow);
                MinecraftClient.getInstance().setScreen(new OrganizablePlayScreensButtonOptionsScreen(parent));
            }).dimensions(width / 2 - 155 + j * 135, y, 40, 20).build();
            resetButtonsBuilder.add(resetButton);
            addDrawableChild(resetButton);
            if (i++ == 4) {
                break;
            }
        }
        resetButtons = resetButtonsBuilder.build();
        options.updateResetButtons();
    }

    /**
     * Adds the option buttons.
     * Overridden to do nothing since the buttons are added in {@link #init()} due to the custom layout.
     */
    @Override
    protected void addOptions() {}

    /**
     * Initializes the footer of the screen.
     * Creates button type button and done button in the footer.
     */
    @Override
    protected void initFooter() {
        GridWidget gridWidget = new GridWidget().setColumnSpacing(10);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(options.buttonType.createWidget(gameOptions));
        adder.add(ButtonWidget.builder(ScreenTexts.DONE, (buttonWidget) -> {
            options.save();
            client.setScreen(parent);
        }).build());
        gridWidget.refreshPositions();
        layout.addFooter(gridWidget);
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
        assert client != null;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, client.world == null ? MENU_LIST_BACKGROUND_TEXTURE : INWORLD_MENU_LIST_BACKGROUND_TEXTURE, 0, layout.getHeaderHeight(), width, height - layout.getFooterHeight(), width, height - layout.getHeaderHeight() - layout.getFooterHeight(), 32, 32);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE, 0, layout.getHeaderHeight() - 2, 0, 0, width, 2, 32, 2);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, 0, height - layout.getFooterHeight(), 0, 0, width, 2, 32, 2);

        super.render(context, mouseX, mouseY, delta);
        int i = 0;
        for (String key : OrganizablePlayScreensOptions.KEYS) {
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable(key), width / 2, MARGIN_TOP - 10 + i, 0xFFFFFFFF);
            i += ROW_HEIGHT;
        }
        if (options.buttonType.getValue()) {
            for (i = 0; i < 5; i++) {
                int y = MARGIN_TOP + i * ROW_HEIGHT;
                int x = width / 2 - 155;
                context.drawTextWithShadow(textRenderer, X_COLON, x + 5, y + 6, 0xFFFFFFFF);
                x = width / 2 - 155 + 135;
                context.drawTextWithShadow(textRenderer, Y_COLON, x + 5, y + 6, 0xFFFFFFFF);
            }
        }
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
