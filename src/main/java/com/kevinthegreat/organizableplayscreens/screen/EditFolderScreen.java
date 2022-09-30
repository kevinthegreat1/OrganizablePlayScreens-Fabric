package com.kevinthegreat.organizableplayscreens.screen;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.Mutable;
import org.lwjgl.glfw.GLFW;

public class EditFolderScreen extends Screen {
    private static final Text ENTER_FOLDER_NAME_TEXT = Text.translatable("organizableplayscreens:folder.enterName");
    private final BooleanConsumer callback;
    private final Mutable<String> folderName;
    private final boolean newFolder;
    private TextFieldWidget nameField;
    private ButtonWidget doneButton;

    public EditFolderScreen(BooleanConsumer callback, Mutable<String> folderName, boolean newFolder) {
        super(Text.translatable("organizableplayscreens:folder.edit"));
        this.callback = callback;
        this.folderName = folderName;
        this.newFolder = newFolder;
    }

    @Override
    protected void init() {
        client.keyboard.setRepeatEvents(true);
        nameField = new TextFieldWidget(textRenderer, width / 2 - 100, 90, 200, 20, ENTER_FOLDER_NAME_TEXT);
        nameField.setMaxLength(128);
        nameField.setTextFieldFocused(true);
        nameField.setText(folderName.getValue());
        nameField.setChangedListener(this::updateDoneButton);
        addSelectableChild(nameField);
        doneButton = addDrawableChild(new ButtonWidget(width / 2 - 100, height / 4 + 96 + 12, 200, 20, ScreenTexts.DONE, buttonWidget -> saveAndClose()));
        addDrawableChild(new ButtonWidget(width / 2 - 100, height / 4 + 120 + 12, 200, 20, ScreenTexts.CANCEL, buttonWidget -> callback.accept(false)));
        updateDoneButton(nameField.getText());
    }

    @Override
    public void tick() {
        nameField.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!doneButton.active || getFocused() != nameField || keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        } else {
            saveAndClose();
            return true;
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String folderName = nameField.getText();
        init(client, width, height);
        nameField.setText(folderName);
    }

    private void saveAndClose() {
        folderName.setValue(nameField.getText());
        callback.accept(true);
    }

    @Override
    public void removed() {
        client.keyboard.setRepeatEvents(false);
    }

    private void updateDoneButton(String text) {
        doneButton.active = newFolder || !folderName.getValue().equals(text);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, title, width / 2, 20, 0xffffff);
        drawTextWithShadow(matrices, textRenderer, ENTER_FOLDER_NAME_TEXT, width / 2 - 100, 80, 0xa0a0a0);
        nameField.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
