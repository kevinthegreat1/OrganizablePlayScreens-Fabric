package com.kevinthegreat.organizableplayscreens.gui.screen;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class OrganizablePlayScreensOptionsScreen extends GameOptionsScreen {
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
        for (SimpleOption<?>[] optionRow : options.asArray()) {
            int j = 0;
            int y = height / 6 + i * 38;
            for (SimpleOption<?> option : optionRow) {
                int x = width / 2 - 155 + j * 135;
                addDrawableChild(option.createButton(gameOptions, x, y, 125));
                j++;
            }
            addDrawableChild(new ButtonWidget(width / 2 - 155 + j * 135, y, 40, 20, Text.translatable("controls.reset"), (buttonWidget) -> {
                options.reset(optionRow);
                clearAndInit();
            }));
            i++;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, title, width / 2, 12, 0xFFFFFF);
        int i = 0;
        for (String key : OrganizablePlayScreensOptions.KEYS) {
            drawCenteredText(matrices, textRenderer, Text.translatable(key), width / 2, height / 6 - 12 + i, 0xFFFFFF);
            i += 38;
        }
        super.render(matrices, mouseX, mouseY, delta);
    }
}
