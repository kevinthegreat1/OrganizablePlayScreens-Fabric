package com.kevinthegreat.organizableplayscreens.gui.screen;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.api.EntryType;
import com.kevinthegreat.organizableplayscreens.gui.AbstractEntry;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDL_DialogFileCallbackI;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

public abstract class AbstractEditEntryScreen<T extends ObjectSelectionList<E>, E extends ObjectSelectionList.Entry<E>> extends Screen {
    private static final SDL_DialogFileFilter.Buffer PNG_FILTERS = SDL_DialogFileFilter.malloc(1)
            .name(MemoryUtil.memUTF8("PNG files"))
            .pattern(MemoryUtil.memUTF8("png"));
    private static final SystemToast.SystemToastId SELECT_ICON = new SystemToast.SystemToastId();
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

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 60);
    private EditBox nameField;
    private final Map<EntryType, Button> entryTypeButtons = new HashMap<>();
    private Button buttonDone;
    private boolean iconChanged;

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
        this(parent, callback, _ -> entry, entry, false);
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
        // Used to clear previous widgets when rebuilding widgets
        layout.removeChildren();
        layout.addTitleHeader(Component.translatable(newEntry ? "organizableplayscreens:entry.new" : "organizableplayscreens:entry.edit", entry.getType().text().getString()), font);

        LinearLayout content = layout.addToContents(LinearLayout.vertical().spacing(12));
        if (newEntry) {
            List<EntryType> entryTypes = getEntryTypes();
            GridLayout.RowHelper adder = content.addChild(new GridLayout(), LayoutSettings::alignHorizontallyCenter).createRowHelper(entryTypes.size());
            for (EntryType entryType : entryTypes) {
                entryTypeButtons.put(entryType, adder.addChild(Button.builder(entryType.text(), _ -> setType(entryType)).width(50).build()));
            }
        }

        Component typeEnterName = Component.translatable("organizableplayscreens:entry.enterName", entry.getType().text().getString()).withColor(0xFFA0A0A0);
        nameField = new EditBox(font, 200, 20, typeEnterName);
        nameField.setMaxLength(128);
        nameField.setFocused(true);
        nameField.setValue(entry.getValue());
        nameField.setResponder(this::updateDoneButton);
        content.addChild(CommonLayouts.labeledElement(font, nameField, typeEnterName));

        LinearLayout iconLayout = content.addChild(LinearLayout.horizontal().spacing(4));
        iconLayout.addChild(Button.builder(Component.translatable("organizableplayscreens:entry.icon.select"), this::selectIcon).width(98).build());
        iconLayout.addChild(Button.builder(Component.translatable("organizableplayscreens:entry.icon.delete"), _ -> minecraft.gui.setScreen(new ConfirmScreen(this::deleteIcon, Component.translatable("organizableplayscreens:entry.deleteEntryQuestion", Component.translatable("organizableplayscreens:entry.icon")), Component.translatable("organizableplayscreens:entry.deleteEntryWarning", Component.translatable("organizableplayscreens:entry.icon")), Component.translatable("organizableplayscreens:entry.icon.delete"), CommonComponents.GUI_CANCEL))).width(98).build());

        LinearLayout footer = layout.addToFooter(LinearLayout.vertical().spacing(4));
        buttonDone = footer.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> saveAndClose()).width(200).build());
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, _ -> callback.accept(false)).width(200).build());

        layout.visitWidgets(this::addRenderableWidget);
        layout.arrangeElements();
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
     * Handles key presses for the screen. Saves and closes the screen if the done button is active, the name text field is not focused, and {@link InputConstants#KEY_RETURN} or {@link InputConstants#KEY_NUMPADENTER} is pressed.
     *
     * @return whether the key press has been consumed or not (prevents further processing or not)
     * @see #saveAndClose()
     */
    @Override
    public boolean keyPressed(@NonNull KeyEvent input) {
        if (!buttonDone.active || getFocused() != nameField || input.key() != InputConstants.KEY_RETURN && input.key() != InputConstants.KEY_NUMPADENTER) {
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
        minecraft.gui.setScreen(parent);
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
        buttonDone.active = newEntry || !entry.getValue().equals(text) || iconChanged;
    }

    private void selectIcon(Button button) {
        SDL_DialogFileCallbackI callback = (_, filelist, _) -> {
            if (filelist == MemoryUtil.NULL) {
                OrganizablePlayScreens.LOGGER.error("Failed to open file dialog: {}", SDLError.SDL_GetError());
                minecraft.execute(() -> minecraft.gui.toastManager().addToast(new SystemToast(SELECT_ICON, Component.translatable("organizableplayscreens:entry.icon.selectFailed"), Component.empty())));
                return;
            }
            long file = MemoryUtil.memGetAddress(filelist);
            if (file == MemoryUtil.NULL) {
                minecraft.execute(() -> minecraft.gui.toastManager().addToast(new SystemToast(SELECT_ICON, Component.translatable("organizableplayscreens:entry.icon.selectedNone"), Component.empty())));
                return;
            }

            loadIcon(MemoryUtil.memUTF8(file));
        };

        SDLDialog.SDL_ShowOpenFileDialog(callback, MemoryUtil.NULL, minecraft.getWindow().handle(), PNG_FILTERS, (CharSequence) null, false);
    }

    private void loadIcon(String path) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return resizeIconImage(NativeImage.read(Files.newInputStream(Path.of(path))));
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, Util.ioPool()).thenAcceptAsync(image -> {
            entry.getCustomIconTexture().upload(image);
            iconChanged = true;
            updateDoneButton(nameField.getValue()); // Needed because the screen stays the same throughout the dialog
            minecraft.gui.toastManager().addToast(new SystemToast(SELECT_ICON, Component.translatable("organizableplayscreens:entry.icon.selectSuccess"), Component.empty()));
        }, minecraft).exceptionallyAsync(e -> {
            OrganizablePlayScreens.LOGGER.error("Failed to read custom icon file for entry {}", entry, e);
            minecraft.gui.toastManager().addToast(new SystemToast(SELECT_ICON, Component.translatable("organizableplayscreens:entry.icon.selectFailed"), Component.empty()));
            return null;
        }, minecraft);
    }

    private NativeImage resizeIconImage(NativeImage image) {
        if (image.getWidth() == 64 && image.getHeight() == 64) return image;

        int size = Math.min(image.getWidth(), image.getHeight());
        int x = (image.getWidth() - size) / 2;
        int y = (image.getHeight() - size) / 2;
        NativeImage scaled = new NativeImage(image.format(), 64, 64, false);
        image.resizeSubRectTo(x, y, size, size, scaled);
        image.close();
        return scaled;
    }

    private void deleteIcon(boolean confirmedAction) {
        if (confirmedAction) {
            entry.getCustomIconTexture().clear();
            iconChanged = true;
        }
        minecraft.gui.setScreen(this);
    }
}
