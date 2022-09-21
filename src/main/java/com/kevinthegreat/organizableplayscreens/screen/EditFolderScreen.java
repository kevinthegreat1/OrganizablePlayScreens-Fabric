package com.kevinthegreat.organizableplayscreens.screen;

import com.kevinthegreat.organizableplayscreens.FolderEntry;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class EditFolderScreen extends Screen {
    private static final Text ENTER_FOLDER_NAME_TEXT = Text.translatable("organizableplayscreens:folder.enterName");
    private final BooleanConsumer callback;
    private final FolderEntry folderEntry;
    private final boolean newFolder;
    private TextFieldWidget nameField;
    private ButtonWidget doneButton;

    public EditFolderScreen(BooleanConsumer callback, FolderEntry folderEntry, boolean newFolder) {
        super(Text.translatable("organizableplayscreens:folder.edit"));
        this.callback = callback;
        this.folderEntry = folderEntry;
        this.newFolder = newFolder;
    }

    @Override
    protected void init() {
        client.keyboard.setRepeatEvents(true);
        nameField = new TextFieldWidget(textRenderer, width / 2 - 100, 90, 200, 20, ENTER_FOLDER_NAME_TEXT);
        nameField.setMaxLength(128);
        nameField.setTextFieldFocused(true);
        nameField.setText(folderEntry.getName());
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
        if (!doneButton.active || getFocused() != nameField || keyCode != 257 && keyCode != 335) {
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
        folderEntry.setName(nameField.getText());
        callback.accept(true);
    }

    @Override
    public void removed() {
        client.keyboard.setRepeatEvents(false);
    }

    private void updateDoneButton(String text) {
        doneButton.active = newFolder || !folderEntry.getName().equals(text);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, title, width / 2, 20, 16777215);
        drawTextWithShadow(matrices, textRenderer, ENTER_FOLDER_NAME_TEXT, width / 2 - 100, 80, 10526880);
        nameField.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }
}