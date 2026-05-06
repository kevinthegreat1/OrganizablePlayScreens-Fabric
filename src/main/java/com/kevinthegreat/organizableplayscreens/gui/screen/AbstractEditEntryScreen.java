package com.kevinthegreat.organizableplayscreens.gui.screen;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.gui.AbstractEntry;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractEditEntryScreen<T extends ObjectSelectionList<E>, E extends ObjectSelectionList.Entry<E>> extends Screen {
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
    private final Function<EntryType, AbstractEntry<T, E>> factory;
    /**
     * The name string to be edited.
     */
    private AbstractEntry<T, E> entry;
    /**
     * Whether a new folder is being created. Allows the done button to be pressed without changing the name if this is true.
     */
    private final boolean newEntry;
    private Component typeTitle;
    private Component typeEnterName;
    private EditBox nameField;
    private final Map<EntryType, Button> entryTypeButtons = new HashMap<>();
    private Button buttonDone;

    /**
     * Creates an edit entry screen for a new entry.
     * @param parent the parent screen
     * @param callback the callback to be called when this screen is closed
     * @param factory the factory to be used to create a new entry of the specific type
     */
    public AbstractEditEntryScreen(Screen parent, BooleanConsumer callback, Function<EntryType, AbstractEntry<T, E>> factory) {
        this(parent, callback, factory, factory.apply(EntryType.FOLDER), true);
    }

    /**
     * Creates an edit entry screen for editing an existing entry.
     * @param parent the parent screen
     * @param callback the callback to be called when this screen is closed
     * @param entry the entry to be edited
     */
    public AbstractEditEntryScreen(Screen parent, BooleanConsumer callback, AbstractEntry<T, E> entry) {
        this(parent, callback, type -> entry, entry, false);
    }

    private AbstractEditEntryScreen(Screen parent, BooleanConsumer callback, Function<EntryType, AbstractEntry<T, E>> factory, AbstractEntry<T, E> entry, boolean newEntry) {
        super(Component.translatable(newEntry ? "organizableplayscreens:entry.new" : "organizableplayscreens:entry.edit"));
        this.parent = parent;
        this.callback = callback;
        this.factory = factory;
        this.entry = entry;
        this.newEntry = newEntry;
    }

    protected abstract List<EntryType> getEntryTypes();

    @Override
    protected void init() {
        if (newEntry) {
            GridLayout gridWidget = new GridLayout();
            List<EntryType> entryTypes = getEntryTypes();
            GridLayout.RowHelper adder = gridWidget.createRowHelper(entryTypes.size());
            for (EntryType entryType : entryTypes) {
                entryTypeButtons.put(entryType, adder.addChild(addRenderableWidget(Button.builder(entryType.text(), buttonWidget_ -> setType(entryType)).width(50).build())));
            }
            gridWidget.arrangeElements();
            FrameLayout.centerInRectangle(gridWidget, 0, 40, width, 40);
        }
        typeTitle = Component.translatable(newEntry ? "organizableplayscreens:entry.new" : "organizableplayscreens:entry.edit", entry.getType().text().getString());
        typeEnterName = Component.translatable("organizableplayscreens:entry.enterName", entry.getType().text().getString());
        nameField = new EditBox(font, width / 2 - 100, 90, 200, 20, typeEnterName);
        nameField.setMaxLength(128);
        nameField.setFocused(true);
        nameField.setValue(entry.getValue());
        nameField.setResponder(this::updateDoneButton);
        addRenderableWidget(nameField);
        buttonDone = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, buttonWidget -> saveAndClose()).bounds(width / 2 - 100, height / 4 + 96 + 12, 200, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, buttonWidget -> callback.accept(false)).bounds(width / 2 - 100, height / 4 + 120 + 12, 200, 20).build());
        updateButtons();
    }

    /**
     * Sets the type of the entry being created.
     *
     * @param entryType the type of the entry
     */
    private void setType(EntryType entryType) {
        entry = factory.apply(entryType);
        nameField.setValue(entry.getValue());
        rebuildWidgets();
    }

    /**
     * Handles key presses for the screen. Saves and closes the screen if the done button is active, the name text field is not focused, and {@link GLFW#GLFW_KEY_ENTER} or {@link GLFW#GLFW_KEY_KP_ENTER} is pressed.
     *
     * @return whether the key press has been consumed or not (prevents further processing or not)
     * @see #saveAndClose()
     */
    @Override
    public boolean keyPressed(KeyEvent input) {
        if (!buttonDone.active || getFocused() != nameField || input.key() != GLFW.GLFW_KEY_ENTER && input.key() != GLFW.GLFW_KEY_KP_ENTER) {
            return super.keyPressed(input);
        } else {
            saveAndClose();
            return true;
        }
    }

    /**
     * Saves the name in the text field when reinitializing the screen.
     */
    @Override
    public void resize(int width, int height) {
        String folderName = nameField.getValue();
        init(width, height);
        nameField.setValue(folderName);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    /**
     * Sets the name to the folder and calls the callback with true.
     */
    private void saveAndClose() {
        entry.setValue(nameField.getValue());
        callback.accept(true);
    }

    /**
     * Updates the button states based on the current entry type.
     */
    private void updateButtons() {
        if (newEntry) {
            EntryType currentType = entry.getType();
            for (Map.Entry<EntryType, Button> entry : entryTypeButtons.entrySet()) {
                entry.getValue().active = entry.getKey() != currentType;
            }
        }
        updateDoneButton(nameField.getValue());
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
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(font, typeTitle, width / 2, 20, 0xFFFFFFFF);
        context.text(font, typeEnterName, width / 2 - 100, 80, 0xFFA0A0A0);
        nameField.extractRenderState(context, mouseX, mouseY, delta);
    }
}
