package com.kevinthegreat.organizableplayscreens.gui.screen;

import com.kevinthegreat.organizableplayscreens.gui.AbstractEntry;
import com.kevinthegreat.organizableplayscreens.gui.EntryType;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.Function;

public class EditEntryScreen extends Screen {
    private final Screen parent;

    /**
     * This is called when this screen should be closed.
     * <p>
     * Calling this with true will save the folder name.
     * Calling this with false will not change anything.
     */
    private final BooleanConsumer callback;
    /**
     * Used to create a new entry of the specific type when the type is changed.
     */
    private final Function<EntryType, AbstractEntry> factory;
    /**
     * The name string to be edited.
     */
    private AbstractEntry entry;
    /**
     * Whether a new folder is being created. Allows the done button to be pressed without changing the name if this is true.
     */
    private final boolean newEntry;
    private Text typeTitle;
    private Text typeEnterName;
    private TextFieldWidget nameField;
    private ButtonWidget buttonFolder;
    private ButtonWidget buttonSection;
    private ButtonWidget buttonSeparator;
    private ButtonWidget buttonDone;

    /**
     * Creates an edit entry screen for a new entry.
     * @param parent the parent screen
     * @param callback the callback to be called when this screen is closed
     * @param factory the factory to be used to create a new entry of the specific type
     */
    public EditEntryScreen(Screen parent, BooleanConsumer callback, Function<EntryType, AbstractEntry> factory) {
        this(parent, callback, factory, factory.apply(EntryType.FOLDER), true);
    }

    /**
     * Creates an edit entry screen for editing an existing entry.
     * @param parent the parent screen
     * @param callback the callback to be called when this screen is closed
     * @param entry the entry to be edited
     */
    public EditEntryScreen(Screen parent, BooleanConsumer callback, AbstractEntry entry) {
        this(parent, callback, type -> entry, entry, false);
    }

    private EditEntryScreen(Screen parent, BooleanConsumer callback, Function<EntryType, AbstractEntry> factory, AbstractEntry entry, boolean newEntry) {
        super(Text.translatable(newEntry ? "organizableplayscreens:entry.new" : "organizableplayscreens:entry.edit"));
        this.parent = parent;
        this.callback = callback;
        this.factory = factory;
        this.entry = entry;
        this.newEntry = newEntry;
    }

    @Override
    protected void init() {
        if (newEntry) {
            buttonFolder = addDrawableChild(ButtonWidget.builder(EntryType.FOLDER.text(), buttonWidget -> setType(EntryType.FOLDER)).dimensions(width / 2 - 75, 40, 50, 20).build());
            buttonSection = addDrawableChild(ButtonWidget.builder(EntryType.SECTION.text(), buttonWidget -> setType(EntryType.SECTION)).dimensions(width / 2 - 25, 40, 50, 20).build());
            buttonSeparator = addDrawableChild(ButtonWidget.builder(EntryType.SEPARATOR.text(), buttonWidget -> setType(EntryType.SEPARATOR)).dimensions(width / 2 + 25, 40, 50, 20).build());
        }
        typeTitle = Text.translatable(newEntry ? "organizableplayscreens:entry.new" : "organizableplayscreens:entry.edit", entry.getType().text().getString());
        typeEnterName = Text.translatable("organizableplayscreens:entry.enterName", entry.getType().text().getString());
        nameField = new TextFieldWidget(textRenderer, width / 2 - 100, 90, 200, 20, typeEnterName);
        nameField.setMaxLength(128);
        nameField.setFocused(true);
        nameField.setText(entry.getValue());
        nameField.setChangedListener(this::updateDoneButton);
        addSelectableChild(nameField);
        buttonDone = addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, buttonWidget -> saveAndClose()).dimensions(width / 2 - 100, height / 4 + 96 + 12, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, buttonWidget -> callback.accept(false)).dimensions(width / 2 - 100, height / 4 + 120 + 12, 200, 20).build());
        updateButtons();
    }

    /**
     * Sets the type of the entry being created.
     *
     * @param entryType the type of the entry
     */
    private void setType(EntryType entryType) {
        entry = factory.apply(entryType);
        nameField.setText(entry.getValue());
        clearAndInit();
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
        if (!buttonDone.active || getFocused() != nameField || keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
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
        entry.setValue(nameField.getText());
        callback.accept(true);
    }

    /**
     * Updates the button states based on the current entry type.
     */
    private void updateButtons() {
        if (newEntry) {
            buttonFolder.active = entry.getType() != EntryType.FOLDER;
            buttonSection.active = entry.getType() != EntryType.SECTION;
            buttonSeparator.active = entry.getType() != EntryType.SEPARATOR;
        }
        updateDoneButton(nameField.getText());
    }

    /**
     * Activates the done button when creating a new folder or when the name has been edited.
     *
     * @param text the text to check for changes
     */
    private void updateDoneButton(String text) {
        buttonDone.active = newEntry || !entry.getValue().equals(text);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, typeTitle, width / 2, 20, 0xffffff);
        context.drawTextWithShadow(textRenderer, typeEnterName, width / 2 - 100, 80, 0xa0a0a0);
        nameField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
}
