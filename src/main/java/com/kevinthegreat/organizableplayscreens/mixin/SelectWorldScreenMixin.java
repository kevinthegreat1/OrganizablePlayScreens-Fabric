package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.gui.AbstractSingleplayerEntry;
import com.kevinthegreat.organizableplayscreens.gui.SingleplayerFolderEntry;
import com.kevinthegreat.organizableplayscreens.gui.WorldListWidgetAccessor;
import com.kevinthegreat.organizableplayscreens.gui.screen.EditEntryScreen;
import com.kevinthegreat.organizableplayscreens.gui.screen.OrganizablePlayScreensOptionsScreen;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("JavadocReference")
@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
    @Shadow
    @Final
    protected Screen parent;
    @Shadow
    private ButtonWidget selectButton;
    @Shadow
    private ButtonWidget recreateButton;
    @Shadow
    protected TextFieldWidget searchBox;
    @Shadow
    private WorldListWidget levelList;
    /**
     * An accessor to access methods in {@link WorldListWidgetMixin}
     */
    public WorldListWidgetAccessor worldListWidgetAccessor;
    /**
     * This is the vanilla cancel button because this is not saved in a vanilla field.
     */
    private ButtonWidget organizableplayscreens_buttonCancel;
    /**
     * This button sets {@link WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one, otherwise to the parent screen.
     */
    private ButtonWidget organizableplayscreens_buttonBack;
    /**
     * This button moves the selected entry to the parent folder.
     */
    private ButtonWidget organizableplayscreens_buttonMoveEntryBack;
    /**
     * This button opens a screen to create a new folder and stores it in {@link #organizableplayscreens_newEntry newFolder}.
     */
    private ButtonWidget organizableplayscreens_buttonNewFolder;
    /**
     * A folder entry to store the folder that is currently being created.
     */
    @Nullable
    private AbstractSingleplayerEntry organizableplayscreens_newEntry;

    protected SelectWorldScreenMixin(Text title) {
        super(title);
    }

    /**
     * Sets the accessor to prevent having to cast it every time.
     */
    @Inject(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/world/SelectWorldScreen;levelList:Lnet/minecraft/client/gui/screen/world/WorldListWidget;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void organizableplayscreens_setWorldListWidgetAccessor(CallbackInfo ci) {
        worldListWidgetAccessor = (WorldListWidgetAccessor) levelList;
    }

    /**
     * Adds 'back', 'move entry back', 'new folder', and 'options' buttons to the screen.
     * <p>
     * The 'back' button sets {@link WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one, otherwise to the parent screen.
     * The 'move entry back' button moves the selected entry to the parent folder.
     * The 'new folder' button opens a screen to create a new folder and stores it in {@link #organizableplayscreens_newEntry newFolder}.
     */
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/SelectWorldScreen;worldSelected(ZZ)V"))
    private void organizableplayscreens_addButtons(CallbackInfo ci) {
        OrganizablePlayScreensOptions options = OrganizablePlayScreens.getInstance().options;
        organizableplayscreens_buttonBack = addDrawableChild(ButtonWidget.builder(Text.of("←"), buttonWidget -> {
            if (!worldListWidgetAccessor.organizableplayscreens_setCurrentFolderToParent()) {
                client.setScreen(parent);
            }
        }).dimensions(options.backButtonX.getValue(), options.backButtonY.getValue(), 20, 20).build());
        organizableplayscreens_buttonMoveEntryBack = addDrawableChild(ButtonWidget.builder(Text.of("←+"), buttonWidget -> {
            if (!worldListWidgetAccessor.organizableplayscreens_isRootFolder()) {
                WorldListWidget.Entry entry = levelList.getSelectedOrNull();
                SingleplayerFolderEntry parentFolder = worldListWidgetAccessor.organizableplayscreens_getCurrentFolder().getParent();
                if (entry instanceof WorldListWidget.WorldEntry worldEntry) {
                    worldListWidgetAccessor.organizableplayscreens_getWorlds().put(worldEntry, parentFolder);
                    parentFolder.getWorldEntries().add(worldEntry);
                    OrganizablePlayScreens.sortWorldEntries(parentFolder.getWorldEntries());
                    worldListWidgetAccessor.organizableplayscreens_getCurrentWorldEntries().remove(worldEntry);
                } else if (entry instanceof AbstractSingleplayerEntry nonWorldEntry) {
                    nonWorldEntry.setParent(parentFolder);
                    parentFolder.getNonWorldEntries().add(nonWorldEntry);
                    worldListWidgetAccessor.organizableplayscreens_getCurrentNonWorldEntries().remove(nonWorldEntry);
                }
                levelList.setSelected(null);
                worldListWidgetAccessor.organizableplayscreens_updateAndSave();
            }
        }).dimensions(options.moveEntryBackButtonX.getValue(), options.moveEntryBackButtonY.getValue(), 20, 20).tooltip(OrganizablePlayScreens.MOVE_ENTRY_BACK_TOOLTIP).build());
        organizableplayscreens_buttonNewFolder = addDrawableChild(ButtonWidget.builder(Text.of("+"), buttonWidget -> {
            client.setScreen(new EditEntryScreen(this, this::organizableplayscreens_addEntry, type -> organizableplayscreens_newEntry = AbstractSingleplayerEntry.of(type, (SelectWorldScreen) (Object) this, worldListWidgetAccessor.organizableplayscreens_getCurrentFolder())));
            levelList.setSelected(organizableplayscreens_newEntry);
        }).dimensions(options.getValue(options.newFolderButtonX), options.newFolderButtonY.getValue(), 20, 20).build());
        addDrawableChild(new TexturedButtonWidget(options.getValue(options.optionsButtonX), options.optionsButtonY.getValue(), 20, 20, 0, 0, 20, OrganizablePlayScreens.OPTIONS_BUTTON_TEXTURE, 32, 64, buttonWidget -> client.setScreen(new OrganizablePlayScreensOptionsScreen(this))));
    }

    /**
     * Modifies the 'select' button to be able to open folders.
     */
    @Inject(method = "method_19945", at = @At("HEAD"), cancellable = true)
    private void organizableplayscreens_modifySelectButton(ButtonWidget button, CallbackInfo ci) {
        if (levelList.getSelectedOrNull() instanceof SingleplayerFolderEntry folderEntry) {
            worldListWidgetAccessor.organizableplayscreens_setCurrentFolder(folderEntry);
            ci.cancel();
        }
    }

    /**
     * Modifies the 'edit' button to work with folders.
     */
    @Inject(method = "method_19943", at = @At("HEAD"), cancellable = true)
    private void organizableplayscreens_modifyEditButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (levelList.getSelectedOrNull() instanceof AbstractSingleplayerEntry entry) {
            client.setScreen(new EditEntryScreen(this, this::organizableplayscreens_editEntry, entry));
            ci.cancel();
        }
    }

    /**
     * Modifies the 'delete' button to work with folders.
     */
    @Inject(method = "method_19942", at = @At("HEAD"), cancellable = true)
    private void organizableplayscreens_modifyDeleteButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (levelList.getSelectedOrNull() instanceof AbstractSingleplayerEntry entry) {
            boolean isFolder = entry instanceof SingleplayerFolderEntry;
            client.setScreen(new ConfirmScreen(this::organizableplayscreens_deleteEntry, Text.translatable("organizableplayscreens:entry.deleteEntryQuestion", entry.getType().text().getString()), Text.translatable(isFolder ? "organizableplayscreens:folder.deleteSingleplayerFolderWarning" : "organizableplayscreens:entry.deleteEntryWarning", entry.getName()), Text.translatable("selectWorld.deleteButton"), ScreenTexts.CANCEL));
            ci.cancel();
        }
    }

    /**
     * Saves the 'cancel' button instance in a field so its text can be changed.
     */
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/SelectWorldScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 5))
    private <T extends Element & Drawable & Selectable> T organizableplayscreens_setCancelButton(SelectWorldScreen selectWorldScreen, T element) {
        organizableplayscreens_buttonCancel = addDrawableChild((ButtonWidget) element);
        return element;
    }

    /**
     * Modifies the 'cancel' button to set {@link WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one and prevent closing the screen.
     */
    @Inject(method = "method_19939", at = @At("HEAD"), cancellable = true)
    private void organizableplayscreens_modifyCancelButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (worldListWidgetAccessor.organizableplayscreens_setCurrentFolderToParent()) {
            ci.cancel();
        }
    }

    /**
     * Adds the non-world entry stored in {@link #organizableplayscreens_newEntry newEntry} to {@link WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder} and sets the screen back to this.
     */
    private void organizableplayscreens_addEntry(boolean confirmedAction) {
        if (confirmedAction) {
            worldListWidgetAccessor.organizableplayscreens_getCurrentNonWorldEntries().add(organizableplayscreens_newEntry);
            worldListWidgetAccessor.organizableplayscreens_updateAndSave();
            organizableplayscreens_newEntry = null;
        }
        client.setScreen(this);
    }

    /**
     * Sets the screen back to this after finishing editing the selected non-world entry.
     */
    private void organizableplayscreens_editEntry(boolean confirmedAction) {
        client.setScreen(this);
    }

    /**
     * Moves the entries inside the selected folder to {@link WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder}, deletes the selected non-world entry, updates the displayed entries, and sets the screen back to this.
     */
    private void organizableplayscreens_deleteEntry(boolean confirmedAction) {
        if (confirmedAction && levelList.getSelectedOrNull() instanceof AbstractSingleplayerEntry nonWorld) {
            if (nonWorld instanceof SingleplayerFolderEntry folder) {
                for (AbstractSingleplayerEntry nonWorldEntry : folder.getNonWorldEntries()) {
                    nonWorldEntry.setParent(worldListWidgetAccessor.organizableplayscreens_getCurrentFolder());
                    worldListWidgetAccessor.organizableplayscreens_getCurrentNonWorldEntries().add(nonWorldEntry);
                }
                for (WorldListWidget.WorldEntry worldEntry : folder.getWorldEntries()) {
                    worldListWidgetAccessor.organizableplayscreens_getWorlds().put(worldEntry, worldListWidgetAccessor.organizableplayscreens_getCurrentFolder());
                    worldListWidgetAccessor.organizableplayscreens_getCurrentWorldEntries().add(worldEntry);
                }
                OrganizablePlayScreens.sortWorldEntries(worldListWidgetAccessor.organizableplayscreens_getCurrentWorldEntries());
            }
            worldListWidgetAccessor.organizableplayscreens_getCurrentNonWorldEntries().remove(nonWorld);
            levelList.setSelected(null);
            worldListWidgetAccessor.organizableplayscreens_updateAndSave();
        }
        client.setScreen(this);
    }

    /**
     * Handles keypress for the screen.
     * <p>
     * Handles escape key first, then the screen buttons, then the search box, and finally the world list widget.
     * First, sets {@link WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one and prevents closing the screen if {@link GLFW#GLFW_KEY_ESCAPE} is pressed.
     * Then, calls {@link Screen#keyPressed(int, int, int)}.
     * Then, calls {@link TextFieldWidget#keyPressed(int, int, int)} for {@code searchBox}.
     * Finally, calls {@link WorldListWidgetMixin#keyPressed(int, int, int)}.
     *
     * @param keyCode the key code that was pressed
     */
    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(keyCode == GLFW.GLFW_KEY_ESCAPE && !shouldCloseOnEsc() && worldListWidgetAccessor.organizableplayscreens_setCurrentFolderToParent() || super.keyPressed(keyCode, scanCode, modifiers) || searchBox.keyPressed(keyCode, scanCode, modifiers) || levelList.keyPressed(keyCode, scanCode, modifiers));
        cir.cancel();
    }

    /**
     * Modifies the y coordinate of the title to fit the current path above it.
     */
    @ModifyConstant(method = "render", constant = @Constant(intValue = 8))
    private int organizableplayscreens_modifyTitleY(int original) {
        return worldListWidgetAccessor.organizableplayscreens_getCurrentPath().isEmpty() ? 8 : 12;
    }

    /**
     * Renders the path of {@link WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder}.
     *
     * @see WorldListWidgetMixin#organizableplayscreens_currentPath currentPath
     */
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawCenteredTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V", shift = At.Shift.AFTER))
    private void organizableplayscreens_renderPath(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.drawCenteredTextWithShadow(textRenderer, worldListWidgetAccessor.organizableplayscreens_getCurrentPath(), width / 2, 2, 0xa0a0a0);
    }

    /**
     * Updates the activation states of buttons. Called at the end of {@link #init()} and every time an entry is selected.
     */
    @Inject(method = "worldSelected", at = @At("RETURN"))
    private void organizableplayscreens_updateButtonStates(boolean buttonsActive, boolean deleteButtonActive, CallbackInfo ci) {
        WorldListWidget.Entry entry = levelList.getSelectedOrNull();
        if (entry instanceof WorldListWidget.WorldEntry) {
            selectButton.setMessage(Text.translatable("selectWorld.select"));
        } else if (entry instanceof AbstractSingleplayerEntry) {
            if (entry instanceof SingleplayerFolderEntry) {
                selectButton.setMessage(Text.translatable("organizableplayscreens:folder.openFolder"));
            } else {
                selectButton.active = false;
            }
            recreateButton.active = false;
        }
        boolean notSearching = searchBox.getText().isEmpty();
        worldListWidgetAccessor.organizableplayscreens_updateCurrentPath();
        searchBox.setY(worldListWidgetAccessor.organizableplayscreens_getCurrentPath().isEmpty() ? 22 : 24);
        organizableplayscreens_buttonBack.active = notSearching;
        organizableplayscreens_buttonMoveEntryBack.active = entry != null && !worldListWidgetAccessor.organizableplayscreens_isRootFolder() && notSearching;
        organizableplayscreens_buttonNewFolder.active = notSearching;
        organizableplayscreens_buttonCancel.setMessage(worldListWidgetAccessor.organizableplayscreens_isRootFolder() ? ScreenTexts.CANCEL : ScreenTexts.BACK);
        for (AbstractSingleplayerEntry nonWorldEntry : worldListWidgetAccessor.organizableplayscreens_getCurrentNonWorldEntries()) {
            nonWorldEntry.updateButtonStates();
        }
    }

    /**
     * Saves the folders and worlds when the screen is closed. Null check necessary as this screen is closed before the world list widget is set when there are no worlds.
     */
    @Override
    public void removed() {
        if (worldListWidgetAccessor != null) {
            worldListWidgetAccessor.organizableplayscreens_saveFile();
        }
        super.removed();
    }

    /**
     * Prevents closing the screen if {@link GLFW#GLFW_KEY_ESCAPE} is pressed and {@link WorldListWidgetMixin#organizableplayscreens_currentFolder currentFolder} is not the root folder
     *
     * @return whether the screen should close
     */
    @Override
    public boolean shouldCloseOnEsc() {
        return worldListWidgetAccessor.organizableplayscreens_isRootFolder();
    }
}
