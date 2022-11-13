package com.kevinthegreat.organizableplayscreens.gui.screen;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.List;

public class OrganizablePlayScreensOptionsScreen extends GameOptionsScreen {
    private static final Text X_COLON = GameOptions.getGenericValueText(OrganizablePlayScreensOptions.X, Text.of(""));
    private static final Text Y_COLON = GameOptions.getGenericValueText(OrganizablePlayScreensOptions.Y, Text.of(""));
    private final OrganizablePlayScreensOptions options;

    public OrganizablePlayScreensOptionsScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("organizableplayscreens:options.title"));
        options = OrganizablePlayScreens.getInstance().options;
    }

    public Screen getParent() {
        return parent;
    }

    @Override
    protected void init() {
        super.init();
        int i = 0;
        for (List<Pair<String, SimpleOption<?>>> optionRow : options.optionsArray) {
            int j = 0;
            int y = height / 6 - 1 + i * 36;
            for (Pair<String, SimpleOption<?>> namedOption : optionRow) {
                int x = width / 2 - 155 + j * 135;
                addDrawableChild(namedOption.getRight().createButton(gameOptions, x, y, 125));
                j++;
            }
            addDrawableChild(new ButtonWidget(width / 2 - 155 + j * 135, y, 40, 20, Text.translatable("controls.reset"), (buttonWidget) -> {
                options.reset(optionRow);
                clearAndInit();
            }));
            if (i++ == 4) {
                break;
            }
        }
        addDrawableChild(options.buttonType.createButton(gameOptions, width / 2 - 155, height - 28, 150));
        addDrawableChild(new ButtonWidget(width / 2 + 5, height - 28, 150, 20, ScreenTexts.DONE, (buttonWidget) -> {
            options.save();
            client.setScreen(parent);
        }));
    }

    public void clearAndInit() {
        super.clearAndInit();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, title, width / 2, 12, 0xFFFFFF);
        int i = 0;
        for (String key : OrganizablePlayScreensOptions.KEYS) {
            drawCenteredText(matrices, textRenderer, Text.translatable(key), width / 2, height / 6 - 12 + i, 0xFFFFFF);
            i += 36;
        }
        if (options.buttonType.getValue()) {
            for (i = 0; i < 5; i++) {
                int y = height / 6 - 1 + i * 36;
                int x = width / 2 - 155;
                drawTextWithShadow(matrices, textRenderer, X_COLON, x + 5, y + 6, 0xFFFFFF);
                x = width / 2 - 155 + 135;
                drawTextWithShadow(matrices, textRenderer, Y_COLON, x + 5, y + 6, 0xFFFFFF);
            }
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        options.save();
    }
}
