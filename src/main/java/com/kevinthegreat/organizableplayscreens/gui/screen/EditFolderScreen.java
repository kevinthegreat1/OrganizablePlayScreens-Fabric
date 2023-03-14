package com.kevinthegreat.organizableplayscreens.gui.screen;

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
    /**
     * This is called when this screen should be closed.
     * <p>
     * Calling this with true will save the folder name.
     * Calling this with false will not change anything.
     */
    private final BooleanConsumer callback;
    /**
     * The name string to be edited.
     */
    private final Mutable<String> folderName;
    /**
     * Whether a new folder is being created. Allows the done button to be pressed without changing the name if this is true.
     */
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
        nameField = new TextFieldWidget(textRenderer, width / 2 - 100, 90, 200, 20, ENTER_FOLDER_NAME_TEXT);
        nameField.setMaxLength(128);
        nameField.setFocused(true);
        nameField.setText(folderName.getValue());
        nameField.setChangedListener(this::updateDoneButton);
        addSelectableChild(nameField);
        doneButton = addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, buttonWidget -> saveAndClose()).dimensions(width / 2 - 100, height / 4 + 96 + 12, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, buttonWidget -> callback.accept(false)).dimensions(width / 2 - 100, height / 4 + 120 + 12, 200, 20).build());
        updateDoneButton(nameField.getText());
    }

    @Override
    public void tick() {
        nameField.tick();
    }

    /**
     * Handles key presses for the screen. Saves and closes the screen if the done button is active, the name text field is not focused, and {@link GLFW#GLFW_KEY_ENTER} or {@link GLFW#GLFW_KEY_KP_ENTER} is pressed.
     *
     * @param keyCode the key code of the key that was pressed
     * @return whether the key press has been consumed or not (prevents further processing or not)
     * @see #saveAndClose()
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!doneButton.active || getFocused() != nameField || keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        } else {
            saveAndClose();
            return true;
        }
    }

    /**
     * Saves the name in the text field when reinitializing the screen.
     */
    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String folderName = nameField.getText();
        init(client, width, height);
        nameField.setText(folderName);
    }

    /**
     * Sets the name to the folder and calls the callback with true.
     */
    private void saveAndClose() {
        folderName.setValue(nameField.getText());
        callback.accept(true);
    }

    /**
     * Activates the done button when creating a new folder or when the name has been edited.
     *
     * @param text the text to check for changes
     */
    private void updateDoneButton(String text) {
        doneButton.active = newFolder || !folderName.getValue().equals(text);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredTextWithShadow(matrices, textRenderer, title, width / 2, 20, 0xffffff);
        drawTextWithShadow(matrices, textRenderer, ENTER_FOLDER_NAME_TEXT, width / 2 - 100, 80, 0xa0a0a0);
        nameField.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
