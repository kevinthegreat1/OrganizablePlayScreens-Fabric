package com.kevinthegreat.organizableplayscreens.gui.screen;

import com.google.common.collect.ImmutableList;
import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.Options;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;

import java.util.List;

public class OrganizablePlayScreensButtonOptionsScreen extends OptionsSubScreen {
    private static final Identifier MENU_LIST_BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");
    private static final int MARGIN_TOP = 46;
    private static final int ROW_HEIGHT = 34;
    /**
     * {@code 'X:'} text used in front of text field options.
     */
    private static final Component X_COLON = Options.genericValueLabel(OrganizablePlayScreensOptions.X, Component.nullToEmpty(""));
    /**
     * {@code 'Y:'} text used in front of text field options.
     */
    private static final Component Y_COLON = Options.genericValueLabel(OrganizablePlayScreensOptions.Y, Component.nullToEmpty(""));
    /**
     * The {@link OrganizablePlayScreensOptions} instance used for loading, storing, and saving the options.
     */
    private final OrganizablePlayScreensOptions modOptions;
    /**
     * The reset buttons ordered by row.
     */
    private List<Button> resetButtons;

    public OrganizablePlayScreensButtonOptionsScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.translatable("organizableplayscreens:options.buttonOptions"));
        modOptions = OrganizablePlayScreens.getInstance().options;
    }

    /**
     * Gets the parent screen.
     * Public for {@link OrganizablePlayScreensOptions} to access potential
     * {@link net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen MultiplayerScreen} and
     * {@link net.minecraft.client.gui.screens.worldselection.SelectWorldScreen SelectWorldScreen} instances.
     *
     * @return the parent screen
     */
    public Screen getParent() {
        return lastScreen;
    }

    /**
     * Initializes the body of the screen.
     * Creates option buttons and reset buttons row by row.
     * Adds the reset buttons to {@link #resetButtons}.
     */
    @Override
    protected void addContents() {
        int i = 0;
        ImmutableList.Builder<Button> resetButtonsBuilder = ImmutableList.builderWithExpectedSize(5);
        for (List<Tuple<String, OptionInstance<?>>> optionRow : modOptions.optionsArray) {
            int j = 0;
            int y = MARGIN_TOP + i * ROW_HEIGHT;
            for (Tuple<String, OptionInstance<?>> namedOption : optionRow) {
                int x = width / 2 - 155 + j * 135;
                addRenderableWidget(namedOption.getB().createButton(options, x, y, 125));
                j++;
            }
            Button resetButton = Button.builder(Component.translatable("controls.reset"), (buttonWidget) -> {
                OrganizablePlayScreensOptions.reset(optionRow);
                Minecraft.getInstance().setScreen(new OrganizablePlayScreensButtonOptionsScreen(lastScreen));
            }).bounds(width / 2 - 155 + j * 135, y, 40, 20).build();
            resetButtonsBuilder.add(resetButton);
            addRenderableWidget(resetButton);
            if (i++ == 4) {
                break;
            }
        }
        resetButtons = resetButtonsBuilder.build();
        modOptions.updateResetButtons();
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
    protected void addFooter() {
        GridLayout gridWidget = new GridLayout().columnSpacing(10);
        GridLayout.RowHelper adder = gridWidget.createRowHelper(2);
        adder.addChild(modOptions.buttonType.createButton(options));
        adder.addChild(Button.builder(CommonComponents.GUI_DONE, (buttonWidget) -> {
            modOptions.save();
            minecraft.setScreen(lastScreen);
        }).build());
        gridWidget.arrangeElements();
        layout.addToFooter(gridWidget);
    }

    /**
     * Renders the screen.
     * Draws the title and option titles. Then, draws the {@code 'X:'} and {@code 'Y:'} text if text fields are being used.
     * Finally, draws the buttons with {@link Screen#render(GuiGraphics, int, int, float) super.render(MatrixStack, int, int, float)}.
     *
     * @param context the draw context
     * @param mouseX  the x position of the mouse
     * @param mouseY  the y position of the mouse
     * @param delta   the time between ticks
     */
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        assert minecraft != null;
        context.blit(RenderPipelines.GUI_TEXTURED, minecraft.level == null ? MENU_LIST_BACKGROUND_TEXTURE : INWORLD_MENU_LIST_BACKGROUND_TEXTURE, 0, layout.getHeaderHeight(), width, height - layout.getFooterHeight(), width, height - layout.getHeaderHeight() - layout.getFooterHeight(), 32, 32);
        context.blit(RenderPipelines.GUI_TEXTURED, minecraft.level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR, 0, layout.getHeaderHeight() - 2, 0, 0, width, 2, 32, 2);
        context.blit(RenderPipelines.GUI_TEXTURED, minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR, 0, height - layout.getFooterHeight(), 0, 0, width, 2, 32, 2);

        super.render(context, mouseX, mouseY, delta);
        int i = 0;
        for (String key : OrganizablePlayScreensOptions.KEYS) {
            context.drawCenteredString(font, Component.translatable(key), width / 2, MARGIN_TOP - 10 + i, 0xFFFFFFFF);
            i += ROW_HEIGHT;
        }
        if (modOptions.buttonType.get()) {
            for (i = 0; i < 5; i++) {
                int y = MARGIN_TOP + i * ROW_HEIGHT;
                int x = width / 2 - 155;
                context.drawString(font, X_COLON, x + 5, y + 6, 0xFFFFFFFF);
                x = width / 2 - 155 + 135;
                context.drawString(font, Y_COLON, x + 5, y + 6, 0xFFFFFFFF);
            }
        }
    }

    /**
     * Saves the options when closing the screen.
     */
    @Override
    public void removed() {
        modOptions.save();
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
