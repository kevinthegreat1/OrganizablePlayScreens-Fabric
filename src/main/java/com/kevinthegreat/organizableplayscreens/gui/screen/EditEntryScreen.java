package com.kevinthegreat.organizableplayscreens.gui.screen;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.function.Function;

public class EditEntryScreen extends Screen {
    private static final Text ENTER_FOLDER_NAME_TEXT = Text.translatable("organizableplayscreens:folder.enterName");
    private final Screen parent;

    /**
     * This is called when this screen should be closed.
     * <p>
     * Calling this with true will save the folder name.
     * Calling this with false will not change anything.
     */
    private final BooleanConsumer callback;
    @Nullable
    private final Function<Type, Mutable<String>> factory;
    /**
     * The name string to be edited.
     */
    private final Mutable<String> entryName;
    /**
     * Whether a new folder is being created. Allows the done button to be pressed without changing the name if this is true.
     */
    private final boolean newEntry;
    private TextFieldWidget nameField;
    private ButtonWidget doneButton;

    public EditEntryScreen(Screen parent, BooleanConsumer callback, Function<Type, Mutable<String>> factory) {
        this(parent, callback, factory, factory.apply(Type.FOLDER), true);
    }

    public EditEntryScreen(Screen parent, BooleanConsumer callback, Mutable<String> entryName) {
        this(parent, callback, null, entryName, false);
    }

    private EditEntryScreen(Screen parent, BooleanConsumer callback, @Nullable Function<Type, Mutable<String>> factory, Mutable<String> entryName, boolean newEntry) {
        super(Text.translatable(newEntry ? "organizableplayscreens:folder.newFolder" : "organizableplayscreens:folder.edit"));
        this.parent = parent;
        this.callback = callback;
        this.factory = factory;
        this.entryName = entryName;
        this.newEntry = newEntry;
    }

    @Override
    protected void init() {
        nameField = new TextFieldWidget(textRenderer, width / 2 - 100, 90, 200, 20, ENTER_FOLDER_NAME_TEXT);
        nameField.setMaxLength(128);
        nameField.setFocused(true);
        nameField.setText(entryName.getValue());
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

    @Override
    public void close() {
        client.setScreen(parent);
    }

    /**
     * Sets the name to the folder and calls the callback with true.
     */
    private void saveAndClose() {
        entryName.setValue(nameField.getText());
        callback.accept(true);
    }

    /**
     * Activates the done button when creating a new folder or when the name has been edited.
     *
     * @param text the text to check for changes
     */
    private void updateDoneButton(String text) {
        doneButton.active = newEntry || !entryName.getValue().equals(text);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xffffff);
        context.drawTextWithShadow(textRenderer, ENTER_FOLDER_NAME_TEXT, width / 2 - 100, 80, 0xa0a0a0);
        nameField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    public enum Type {
        FOLDER
    }
}
