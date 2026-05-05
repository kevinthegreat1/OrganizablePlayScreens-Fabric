package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.gui.AbstractEntry;
import com.kevinthegreat.organizableplayscreens.gui.AbstractSingleplayerEntry;
import com.kevinthegreat.organizableplayscreens.gui.SingleplayerFolderEntry;
import com.kevinthegreat.organizableplayscreens.gui.screen.OrganizablePlayScreensOptionsScreen;
import com.kevinthegreat.organizableplayscreens.gui.screen.SingleplayerEditEntryScreen;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("JavadocReference")
@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
    @Shadow
    @Final
    protected Screen lastScreen;
    @Shadow
    private WorldSelectionList list;
    @Shadow
    private Button selectButton;
    @Shadow
    private Button copyButton;
    @Shadow
    protected EditBox searchBox;
    @Shadow
    private Button renameButton;
    @Shadow
    private Button deleteButton;

    /**
     * This is the vanilla cancel button because this is not saved in a vanilla field.
     */
    @Unique
    private Button organizableplayscreens_buttonCancel;
    /**
     * This button sets {@link com.kevinthegreat.organizableplayscreens.mixin.WorldSelectionListMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one, otherwise to the parent screen.
     */
    @Unique
    private Button organizableplayscreens_buttonBack;
    /**
     * This button moves the selected entry to the parent folder.
     */
    @Unique
    private Button organizableplayscreens_buttonMoveEntryBack;
    /**
     * This button opens a screen to create a new folder and stores it in {@link #organizableplayscreens_newEntry newEntry}.
     */
    @Unique
    private Button organizableplayscreens_buttonNewEntry;
    /**
     * This button opens the options screen.
     */
    @Unique
    private Button organizableplayscreens_buttonOptions;
    /**
     * The path of the {@link com.kevinthegreat.organizableplayscreens.mixin.WorldSelectionListMixin#organizableplayscreens_currentFolder currentFolder}.
     * <p>
     * Only used for display. In the form of '{@code folder > child folder}'. Empty in the root folder.
     */
    @Unique
    private StringWidget organizableplayscreens_pathWidget;
    /**
     * A folder entry to store the folder that is currently being created.
     */
    @Unique
    @Nullable
    private AbstractSingleplayerEntry organizableplayscreens_newEntry;

    protected SelectWorldScreenMixin(Component title) {
        super(title);
    }

    /**
     * Adds the path of the {@link com.kevinthegreat.organizableplayscreens.mixin.WorldSelectionListMixin#organizableplayscreens_currentFolder currentFolder} to the header of the screen.
     *
     * @see #organizableplayscreens_pathWidget pathWidget
     */
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 0))
    private void organizableplayscreens_modifyHeader(CallbackInfo ci, @Local(ordinal = 0) LinearLayout headerLayout) {
        if (organizableplayscreens_pathWidget == null) organizableplayscreens_pathWidget = new StringWidget(Component.empty(), font);
        headerLayout.addChild(organizableplayscreens_pathWidget);
    }

    /**
     * Adds the 'back', 'move entry back', 'new folder', and 'options' buttons to the screen.
     * <p>
     * The 'back' button sets {@link com.kevinthegreat.organizableplayscreens.mixin.WorldSelectionListMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one, otherwise to the parent screen.
     * The 'move entry back' button moves the selected entry to the parent folder.
     * The 'new folder' button opens a screen to create a new folder and stores it in {@link #organizableplayscreens_newEntry newFolder}.
     */
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/SelectWorldScreen;createFooterButtons(Ljava/util/function/Consumer;Lnet/minecraft/client/gui/screens/worldselection/WorldSelectionList;)V"))
    private void organizableplayscreens_addButtons(CallbackInfo ci) {
        list.organizableplayscreens_setPathWidget(organizableplayscreens_pathWidget);

        OrganizablePlayScreensOptions options = OrganizablePlayScreens.getInstance().options;

        organizableplayscreens_buttonBack = addRenderableWidget(Button.builder(Component.nullToEmpty("←"), buttonWidget -> {
            if (!list.organizableplayscreens_setCurrentFolderToParent()) {
                minecraft.setScreen(lastScreen);
            }
        }).bounds(options.backButtonX.get(), options.backButtonY.get(), 20, 20).build());
        organizableplayscreens_buttonMoveEntryBack = addRenderableWidget(Button.builder(Component.nullToEmpty("←+"), buttonWidget -> {
            if (!list.organizableplayscreens_isRootFolder()) {
                WorldSelectionList.Entry entry = list.getSelected();
                SingleplayerFolderEntry parentFolder = list.organizableplayscreens_getCurrentFolder().getParent();
                if (entry instanceof WorldSelectionList.WorldListEntry worldEntry) {
                    list.organizableplayscreens_getWorlds().put(worldEntry, parentFolder);
                    parentFolder.getWorldEntries().add(worldEntry);
                    OrganizablePlayScreens.sortWorldEntries(parentFolder.getWorldEntries());
                    list.organizableplayscreens_getCurrentWorldEntries().remove(worldEntry);
                } else if (entry instanceof AbstractSingleplayerEntry nonWorldEntry) {
                    nonWorldEntry.setParent(parentFolder);
                    parentFolder.getNonWorldEntries().add(nonWorldEntry);
                    list.organizableplayscreens_getCurrentNonWorldEntries().remove(nonWorldEntry);
                }
                list.setSelected(null);
                list.organizableplayscreens_updateAndSave();
            }
        }).bounds(options.moveEntryBackButtonX.get(), options.moveEntryBackButtonY.get(), 20, 20).tooltip(OrganizablePlayScreens.MOVE_ENTRY_BACK_TOOLTIP).build());
        organizableplayscreens_buttonNewEntry = addRenderableWidget(Button.builder(Component.nullToEmpty("+"), buttonWidget -> minecraft.setScreen(new SingleplayerEditEntryScreen(this, this::organizableplayscreens_addEntry, type -> {
            SingleplayerFolderEntry folder = list.organizableplayscreens_getCurrentFolder();
            return organizableplayscreens_newEntry = type.singleplayerEntry((SelectWorldScreen) (Object) this, folder);
        }))).bounds(options.getValue(options.newFolderButtonX), options.newFolderButtonY.get(), 20, 20).build());
        organizableplayscreens_buttonOptions = addRenderableWidget(new LegacyTexturedButtonWidget(options.getValue(options.optionsButtonX), options.optionsButtonY.get(), 20, 20, 0, 0, 20, OrganizablePlayScreens.OPTIONS_BUTTON_TEXTURE, 32, 64, buttonWidget -> minecraft.setScreen(new OrganizablePlayScreensOptionsScreen(this)), Component.translatable("organizableplayscreens:options.optionsButton")));
    }

    /**
     * Updates the positions of the added buttons according to the options. Called when the screen is reinitialized or resized.
     */
    @Inject(method = "repositionElements", at = @At("RETURN"))
    private void organizableplayscreens_refreshWidgetPositions(CallbackInfo ci) {
        OrganizablePlayScreensOptions options = OrganizablePlayScreens.getInstance().options;
        organizableplayscreens_buttonBack.setPosition(options.backButtonX.get(), options.backButtonY.get());
        organizableplayscreens_buttonMoveEntryBack.setPosition(options.moveEntryBackButtonX.get(), options.moveEntryBackButtonY.get());
        organizableplayscreens_buttonNewEntry.setPosition(options.getValue(options.newFolderButtonX), options.newFolderButtonY.get());
        organizableplayscreens_buttonOptions.setPosition(options.getValue(options.optionsButtonX), options.optionsButtonY.get());
    }

    /**
     * Modifies the 'select' button to be able to open folders.
     */
    @Inject(method = "method_74220", at = @At("HEAD"), cancellable = true)
    private static void organizableplayscreens_modifySelectButton(CallbackInfo ci, @Local(argsOnly = true) WorldSelectionList levelList) {
        if (levelList.getSelected() instanceof SingleplayerFolderEntry folderEntry) {
            levelList.organizableplayscreens_setCurrentFolder(folderEntry);
            ci.cancel();
        }
    }

    /**
     * Modifies the 'edit' button to work with folders.
     */
    @Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
    @Definition(id = "translatable", method = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    @Expression("builder(translatable('selectWorld.edit'), ?)")
    @ModifyArg(method = "createFooterButtons", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Button.OnPress organizableplayscreens_modifyEditButton(Button.OnPress editAction) {
        return button -> {
            if (list.getSelected() instanceof AbstractSingleplayerEntry entry) {
                minecraft.setScreen(new SingleplayerEditEntryScreen(this, this::organizableplayscreens_editEntry, entry));
            } else editAction.onPress(button);
        };
    }

    /**
     * Modifies the 'delete' button to work with folders.
     */
    @Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
    @Definition(id = "translatable", method = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    @Expression("builder(translatable('selectWorld.delete'), ?)")
    @ModifyArg(method = "createFooterButtons", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Button.OnPress organizableplayscreens_modifyDeleteButton(Button.OnPress deleteAction) {
        return button -> {
            if (list.getSelected() instanceof AbstractSingleplayerEntry entry) {
                boolean isFolder = entry instanceof SingleplayerFolderEntry;
                minecraft.setScreen(new ConfirmScreen(this::organizableplayscreens_deleteEntry, Component.translatable("organizableplayscreens:entry.deleteEntryQuestion", entry.getType().text().getString()), Component.translatable(isFolder ? "organizableplayscreens:folder.deleteSingleplayerFolderWarning" : "organizableplayscreens:entry.deleteEntryWarning", entry.getName()), Component.translatable("selectWorld.deleteButton"), CommonComponents.GUI_CANCEL));
            } else deleteAction.onPress(button);
        };
    }

    /**
     * Saves the 'cancel' button instance in a field so its text can be changed.
     */
    @ModifyExpressionValue(method = "createFooterButtons", slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/network/chat/CommonComponents;GUI_BACK:Lnet/minecraft/network/chat/Component;", opcode = Opcodes.GETSTATIC)), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;", ordinal = 0))
    private Button organizableplayscreens_setCancelButton(Button buttonCancel) {
        return organizableplayscreens_buttonCancel = buttonCancel;
    }

    /**
     * Modifies the 'cancel' button to set {@link com.kevinthegreat.organizableplayscreens.mixin.WorldSelectionListMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one and prevent closing the screen.
     */
    @Inject(method = "method_74222", at = @At("HEAD"), cancellable = true)
    private void organizableplayscreens_modifyCancelButton(CallbackInfo ci) {
        if (list.organizableplayscreens_setCurrentFolderToParent()) {
            ci.cancel();
        }
    }

    /**
     * Adds the non-world entry stored in {@link #organizableplayscreens_newEntry newEntry} to {@link com.kevinthegreat.organizableplayscreens.mixin.WorldSelectionListMixin#organizableplayscreens_currentFolder currentFolder} and sets the screen back to this.
     */
    @Unique
    private void organizableplayscreens_addEntry(boolean confirmedAction) {
        if (confirmedAction) {
            list.organizableplayscreens_getCurrentNonWorldEntries().add(organizableplayscreens_newEntry);
            list.organizableplayscreens_updateAndSave();
            list.setSelected(organizableplayscreens_newEntry);
            organizableplayscreens_newEntry = null;
        }
        minecraft.setScreen(this);
    }

    /**
     * Sets the screen back to this after finishing editing the selected non-world entry.
     */
    @Unique
    private void organizableplayscreens_editEntry(boolean confirmedAction) {
        minecraft.setScreen(this);
    }

    /**
     * Moves the entries inside the selected folder to {@link com.kevinthegreat.organizableplayscreens.mixin.WorldSelectionListMixin#organizableplayscreens_currentFolder currentFolder}, deletes the selected non-world entry, updates the displayed entries, and sets the screen back to this.
     */
    @Unique
    private void organizableplayscreens_deleteEntry(boolean confirmedAction) {
        if (confirmedAction && list.getSelected() instanceof AbstractSingleplayerEntry nonWorld) {
            if (nonWorld instanceof SingleplayerFolderEntry folder) {
                for (AbstractSingleplayerEntry nonWorldEntry : folder.getNonWorldEntries()) {
                    nonWorldEntry.setParent(list.organizableplayscreens_getCurrentFolder());
                    list.organizableplayscreens_getCurrentNonWorldEntries().add(nonWorldEntry);
                }
                for (WorldSelectionList.WorldListEntry worldEntry : folder.getWorldEntries()) {
                    list.organizableplayscreens_getWorlds().put(worldEntry, list.organizableplayscreens_getCurrentFolder());
                    list.organizableplayscreens_getCurrentWorldEntries().add(worldEntry);
                }
                OrganizablePlayScreens.sortWorldEntries(list.organizableplayscreens_getCurrentWorldEntries());
            }
            list.organizableplayscreens_getCurrentNonWorldEntries().remove(nonWorld);
            list.setSelected(null);
            list.organizableplayscreens_updateAndSave();
        }
        minecraft.setScreen(this);
    }

    /**
     * Handles escape key and the screen buttons.
     * <p>
     * First, sets {@link com.kevinthegreat.organizableplayscreens.mixin.WorldSelectionListMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one and prevents closing the screen if {@link GLFW#GLFW_KEY_ESCAPE} is pressed.
     * Then, calls {@link Screen#keyPressed(int, int, int)}.
     */
    @Override
    public boolean keyPressed(KeyEvent input) {
        return input.key() == GLFW.GLFW_KEY_ESCAPE && !shouldCloseOnEsc() && list.organizableplayscreens_setCurrentFolderToParent() || super.keyPressed(input);
    }

    /**
     * Updates the activation states of buttons. Called at the end of {@link #init()} and every time an entry is selected.
     */
    @Inject(method = "updateButtonStatus", at = @At("RETURN"))
    private void organizableplayscreens_updateButtonStates(LevelSummary levelSummary, CallbackInfo ci) {
        WorldSelectionList.Entry selectedEntry = list.getSelected();
        if (selectedEntry instanceof WorldSelectionList.WorldListEntry) {
            selectButton.setMessage(Component.translatable("selectWorld.select"));
        } else if (selectedEntry instanceof AbstractEntry<?, ?> abstractEntry) {
            abstractEntry.updateScreenButtonStates(selectButton, renameButton, deleteButton, copyButton);
        }
        boolean notSearching = searchBox.getValue().isEmpty();
        organizableplayscreens_buttonBack.active = notSearching;
        organizableplayscreens_buttonMoveEntryBack.active = selectedEntry != null && !list.organizableplayscreens_isRootFolder() && notSearching;
        organizableplayscreens_buttonNewEntry.active = notSearching;
        organizableplayscreens_buttonCancel.setMessage(list.organizableplayscreens_isRootFolder() ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_BACK);
        for (AbstractSingleplayerEntry nonWorldEntry : list.organizableplayscreens_getCurrentNonWorldEntries()) {
            nonWorldEntry.updateButtonStates(selectedEntry);
        }
    }

    /**
     * Saves the folders and worlds when the screen is closed. Null check necessary as this screen is closed before the world list widget is set when there are no worlds.
     */
    @Inject(method = "removed", at = @At("RETURN"))
    private void organizableplayscreens_removed(CallbackInfo ci) {
        if (list != null) {
            list.organizableplayscreens_saveFile();
        }
    }

    /**
     * Prevents closing the screen if {@link GLFW#GLFW_KEY_ESCAPE} is pressed and {@link com.kevinthegreat.organizableplayscreens.mixin.WorldSelectionListMixin#organizableplayscreens_currentFolder currentFolder} is not the root folder
     *
     * @return whether the screen should close
     */
    @Override
    public boolean shouldCloseOnEsc() {
        return list.organizableplayscreens_isRootFolder();
    }
}
