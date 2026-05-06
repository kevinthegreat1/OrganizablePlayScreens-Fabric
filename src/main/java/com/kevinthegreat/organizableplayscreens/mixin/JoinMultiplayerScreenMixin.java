package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.compatibility.Compatibility;
import com.kevinthegreat.organizableplayscreens.gui.AbstractEntry;
import com.kevinthegreat.organizableplayscreens.gui.AbstractMultiplayerEntry;
import com.kevinthegreat.organizableplayscreens.gui.MultiplayerFolderEntry;
import com.kevinthegreat.organizableplayscreens.gui.screen.MultiplayerEditEntryScreen;
import com.kevinthegreat.organizableplayscreens.gui.screen.OrganizablePlayScreensOptionsScreen;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("JavadocReference")
@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin extends Screen {
    @Shadow
    @Final
    private Screen lastScreen;
    @Shadow
    protected ServerSelectionList serverSelectionList;
    @Shadow
    private Button selectButton;
    @Shadow
    private Button editButton;
    @Shadow
    private Button deleteButton;
    @Shadow
    private ServerData editingServer;

    /**
     * This is the vanilla cancel button because this is not saved in a vanilla field.
     */
    @Unique
    private Button organizableplayscreens_buttonCancel;
    /**
     * This button sets {@link com.kevinthegreat.organizableplayscreens.mixin.ServerSelectionListMixin#organizableplayscreens_currentFolder currentFolder} to its parent folder if there is one, otherwise to the parent screen.
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
     * The path of the {@link com.kevinthegreat.organizableplayscreens.mixin.ServerSelectionListMixin#organizableplayscreens_currentFolder currentFolder}.
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
    private AbstractMultiplayerEntry organizableplayscreens_newEntry;
    /**
     * A field to mark whether multiplayer features should be prevented in this instance.
     * We mark the instance instead of check at the beginning of methods
     * because {@link #removed()} runs after essential sets the multiplayer tab to the new value,
     * and checking the tab would yield an incorrect result.
     */
    @Unique
    private boolean preventMultiplayerFeatures;

    protected JoinMultiplayerScreenMixin(Component title) {
        super(title);
    }

    /**
     * Adds the path of the {@link com.kevinthegreat.organizableplayscreens.mixin.ServerSelectionListMixin#organizableplayscreens_currentFolder currentFolder} to the header of the screen.
     *
     * @see #organizableplayscreens_pathWidget pathWidget
     */
    @WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addTitleHeader(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/Font;)V"))
    private void organizableplayscreens_modifyHeader(HeaderAndFooterLayout layout, Component text, Font textRenderer, Operation<Void> original) {
        if (Compatibility.essential_preventMultiplayerFeatures()) {
            preventMultiplayerFeatures = true;
            return;
        }

        LinearLayout headerLayout = layout.addToHeader(LinearLayout.vertical().spacing(4));
        headerLayout.defaultCellSetting().alignHorizontallyCenter();
        if (organizableplayscreens_pathWidget == null) organizableplayscreens_pathWidget = new StringWidget(Component.empty(), textRenderer);
        headerLayout.addChild(organizableplayscreens_pathWidget);
        headerLayout.addChild(new StringWidget(text, textRenderer));
    }

    /**
     * Loads and displays folders and servers from {@code organizable_servers.dat} and adds 'back', 'move entry back', 'new folder', and 'options' buttons to the screen.
     * <p>
     * The 'back' button sets {@link com.kevinthegreat.organizableplayscreens.mixin.ServerSelectionListMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one, otherwise to the parent screen.
     * The 'move entry back' button moves the selected entry to the parent folder.
     * The 'new folder' button opens a screen to create a new folder and stores it in {@link #organizableplayscreens_newEntry newFolder}.
     */
    @WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/multiplayer/ServerSelectionList;updateOnlineServers(Lnet/minecraft/client/multiplayer/ServerList;)V"))
    private void organizableplayscreens_loadFileAndAddButtons(ServerSelectionList serverListWidget, ServerList servers, Operation<Void> original) {
        if (Compatibility.essential_preventMultiplayerFeatures()) {
            preventMultiplayerFeatures = true;
            return;
        }

        serverListWidget.organizableplayscreens_setPathWidget(organizableplayscreens_pathWidget);
        original.call(serverListWidget, servers);
        serverListWidget.organizableplayscreens_loadFile();

        OrganizablePlayScreensOptions options = OrganizablePlayScreens.getInstance().options;

        organizableplayscreens_buttonBack = addRenderableWidget(Button.builder(Component.nullToEmpty("←"), buttonWidget -> {
            if (!serverListWidget.organizableplayscreens_setCurrentFolderToParent()) {
                minecraft.setScreen(lastScreen);
            }
        }).bounds(options.backButtonX.get(), options.backButtonY.get(), 20, 20).build());
        organizableplayscreens_buttonMoveEntryBack = addRenderableWidget(Button.builder(Component.nullToEmpty("←+"), buttonWidget -> {
            if (!serverListWidget.organizableplayscreens_isRootFolder()) {
                ServerSelectionList.Entry entry = serverListWidget.getSelected();
                if (entry != null) {
                    MultiplayerFolderEntry parentFolder = serverListWidget.organizableplayscreens_getCurrentFolder().getParent();
                    if (entry instanceof AbstractMultiplayerEntry nonServerEntry) {
                        nonServerEntry.setParent(parentFolder);
                    }
                    parentFolder.getEntries().add(entry);
                    serverListWidget.organizableplayscreens_getCurrentEntries().remove(entry);
                    serverListWidget.organizableplayscreens_updateAndSave();
                }
            }
        }).bounds(options.moveEntryBackButtonX.get(), options.moveEntryBackButtonY.get(), 20, 20).tooltip(OrganizablePlayScreens.MOVE_ENTRY_BACK_TOOLTIP).build());
        organizableplayscreens_buttonNewEntry = addRenderableWidget(Button.builder(Component.nullToEmpty("+"), buttonWidget -> minecraft.setScreen(new MultiplayerEditEntryScreen(this, this::organizableplayscreens_addEntry, type -> {
            MultiplayerFolderEntry folder = serverListWidget.organizableplayscreens_getCurrentFolder();
            return organizableplayscreens_newEntry = type.multiplayerEntry((JoinMultiplayerScreen) (Object) this, folder);
        }))).bounds(options.getValue(options.newFolderButtonX), options.newFolderButtonY.get(), 20, 20).build());
        organizableplayscreens_buttonOptions = addRenderableWidget(new LegacyTexturedButtonWidget(options.getValue(options.optionsButtonX), options.optionsButtonY.get(), 20, 20, 0, 0, 20, OrganizablePlayScreens.OPTIONS_BUTTON_TEXTURE, 32, 64, buttonWidget -> minecraft.setScreen(new OrganizablePlayScreensOptionsScreen(this)), Component.translatable("organizableplayscreens:options.optionsButton")));
    }

    /**
     * Updates the positions of the added buttons according to the options. Called when the screen is reinitialized or resized.
     */
    @Inject(method = "repositionElements", at = @At(value = "RETURN"))
    private void organizableplayscreens_refreshWidgetPositions(CallbackInfo ci) {
        OrganizablePlayScreensOptions options = OrganizablePlayScreens.getInstance().options;
        if (organizableplayscreens_buttonBack != null) organizableplayscreens_buttonBack.setPosition(options.backButtonX.get(), options.backButtonY.get());
        if (organizableplayscreens_buttonMoveEntryBack != null) organizableplayscreens_buttonMoveEntryBack.setPosition(options.moveEntryBackButtonX.get(), options.moveEntryBackButtonY.get());
        if (organizableplayscreens_buttonNewEntry != null) organizableplayscreens_buttonNewEntry.setPosition(options.getValue(options.newFolderButtonX), options.newFolderButtonY.get());
        if (organizableplayscreens_buttonOptions != null) organizableplayscreens_buttonOptions.setPosition(options.getValue(options.optionsButtonX), options.optionsButtonY.get());
    }

    /**
     * Modifies the 'edit' button to work with folders.
     */
    @Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
    @Definition(id = "translatable", method = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    @Expression("builder(translatable('selectServer.edit'), ?)")
    @ModifyArg(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private Button.OnPress organizableplayscreens_modifyEditButton(Button.OnPress editAction) {
        return button -> {
            editAction.onPress(button);
            if (serverSelectionList.getSelected() instanceof AbstractMultiplayerEntry entry) {
                minecraft.setScreen(new MultiplayerEditEntryScreen(this, this::organizableplayscreens_editEntry, entry));
            }
        };
    }

    /**
     * Modifies the 'delete' button to work with folders.
     */
    @Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
    @Definition(id = "translatable", method = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    @Expression("builder(translatable('selectServer.delete'), ?)")
    @ModifyArg(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private Button.OnPress organizableplayscreens_modifyDeleteButton(Button.OnPress deleteAction) {
        return button -> {
            deleteAction.onPress(button);
            if (serverSelectionList.getSelected() instanceof AbstractMultiplayerEntry entry) {
                boolean isFolder = entry instanceof MultiplayerFolderEntry;
                minecraft.setScreen(new ConfirmScreen(this::organizableplayscreens_deleteEntry, Component.translatable("organizableplayscreens:entry.deleteEntryQuestion", entry.getType().text().getString()), Component.translatable(isFolder ? "organizableplayscreens:folder.deleteMultiplayerFolderWarning" : "organizableplayscreens:entry.deleteEntryWarning", entry.getName()), Component.translatable("selectServer.deleteButton"), CommonComponents.GUI_CANCEL));
            }
        };
    }

    /**
     * Saves the 'cancel' button instance in a field so its text can be changed.
     */
    @ModifyExpressionValue(method = "init", slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/network/chat/CommonComponents;GUI_BACK:Lnet/minecraft/network/chat/Component;", opcode = Opcodes.GETSTATIC)), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;", ordinal = 0))
    private Button organizableplayscreens_setCancelButton(Button buttonCancel) {
        return organizableplayscreens_buttonCancel = buttonCancel;
    }

    /**
     * Modifies the 'cancel' button to set {@link com.kevinthegreat.organizableplayscreens.mixin.ServerSelectionListMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one and prevent closing the screen.
     */
    @Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
    @Definition(id = "GUI_BACK", field = "Lnet/minecraft/network/chat/CommonComponents;GUI_BACK:Lnet/minecraft/network/chat/Component;")
    @Expression("builder(GUI_BACK, ?)")
    @ModifyArg(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private Button.OnPress organizableplayscreens_modifyCancelButton(Button.OnPress cancelAction) {
        return button -> {
            if (!serverSelectionList.organizableplayscreens_setCurrentFolderToParent()) cancelAction.onPress(button);
        };
    }

    /**
     * Adds the non-server entry stored in {@link #organizableplayscreens_newEntry newEntry} to {@link com.kevinthegreat.organizableplayscreens.mixin.ServerSelectionListMixin#organizableplayscreens_currentFolder currentFolder} and sets the screen back to this.
     */
    @Unique
    private void organizableplayscreens_addEntry(boolean confirmedAction) {
        if (confirmedAction) {
            serverSelectionList.organizableplayscreens_getCurrentEntries().add(organizableplayscreens_newEntry);
            serverSelectionList.organizableplayscreens_updateAndSave();
            serverSelectionList.setSelected(organizableplayscreens_newEntry);
            organizableplayscreens_newEntry = null;
        }
        minecraft.setScreen(this);
    }

    /**
     * Sets the screen back to this after finishing editing the selected non-server entry.
     */
    @Unique
    private void organizableplayscreens_editEntry(boolean confirmedAction) {
        minecraft.setScreen(this);
    }

    /**
     * Deletes the selected non-server entry, updates the displayed entries, and sets the screen back to this.
     */
    @Unique
    private void organizableplayscreens_deleteEntry(boolean confirmedAction) {
        if (confirmedAction) {
            serverSelectionList.organizableplayscreens_getCurrentEntries().remove(serverSelectionList.getSelected());
            serverSelectionList.setSelected(null);
            serverSelectionList.organizableplayscreens_updateAndSave();
        }
        minecraft.setScreen(this);
    }

    /**
     * Adds the server in {@link JoinMultiplayerScreen#editingServer selectedEntry} to {@link com.kevinthegreat.organizableplayscreens.mixin.ServerSelectionListMixin#organizableplayscreens_currentFolder currentFolder}.
     */
    @Inject(method = "addServerCallback", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/multiplayer/ServerSelectionList;updateOnlineServers(Lnet/minecraft/client/multiplayer/ServerList;)V", shift = At.Shift.AFTER))
    private void organizableplayscreens_addServer(boolean confirmedAction, CallbackInfo ci) {
        serverSelectionList.organizableplayscreens_getCurrentEntries().add(ServerSelectionListMixin.ServerEntryAccessor.create(serverSelectionList, (JoinMultiplayerScreen) (Object) this, editingServer));
        serverSelectionList.organizableplayscreens_updateAndSave();
    }

    /**
     * Edits the selected server to match {@link JoinMultiplayerScreen#editingServer selectedEntry} in {@link com.kevinthegreat.organizableplayscreens.mixin.ServerSelectionListMixin#organizableplayscreens_currentFolder currentFolder}.
     */
    @Inject(method = "editServerCallback", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/multiplayer/ServerSelectionList;updateOnlineServers(Lnet/minecraft/client/multiplayer/ServerList;)V"))
    private void organizableplayscreens_editServer(boolean confirmedAction, CallbackInfo ci) {
        serverSelectionList.organizableplayscreens_getCurrentEntries().set(serverSelectionList.organizableplayscreens_getCurrentEntries().indexOf(serverSelectionList.getSelected()), ServerSelectionListMixin.ServerEntryAccessor.create(serverSelectionList, (JoinMultiplayerScreen) (Object) this, editingServer));
        serverSelectionList.organizableplayscreens_updateAndSave();
    }

    /**
     * Removes the selected server from {@link com.kevinthegreat.organizableplayscreens.mixin.ServerSelectionListMixin#organizableplayscreens_currentFolder currentFolder}.
     */
    @Inject(method = "deleteCallback", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/multiplayer/ServerSelectionList;setSelected(Lnet/minecraft/client/gui/screens/multiplayer/ServerSelectionList$Entry;)V"))
    private void organizableplayscreens_removeServer(boolean confirmedAction, CallbackInfo ci) {
        serverSelectionList.organizableplayscreens_getCurrentEntries().remove(serverSelectionList.getSelected());
        serverSelectionList.organizableplayscreens_updateAndSave();
    }

    /**
     * Opens the selected entry if it is a folder.
     */
    @Inject(method = "join", at = @At(value = "RETURN"))
    private void organizableplayscreens_openFolder(CallbackInfo ci) {
        if (serverSelectionList.getSelected() instanceof AbstractMultiplayerEntry entry) {
            entry.entrySelectionConfirmed(serverSelectionList);
        }
    }

    /**
     * Sets {@link com.kevinthegreat.organizableplayscreens.mixin.ServerSelectionListMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one and prevents closing the screen if {@link GLFW#GLFW_KEY_ESCAPE} is pressed.
     */
    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_keyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE && !shouldCloseOnEsc() && serverSelectionList.organizableplayscreens_setCurrentFolderToParent() && !Compatibility.essential_preventMultiplayerFeatures()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Updates the activation states of buttons. Called at the end of {@link #init()} and every time an entry is selected.
     */
    @Inject(method = "onSelectedChange", at = @At(value = "RETURN"))
    private void organizableplayscreens_updateButtonActivationStates(CallbackInfo ci) {
        if (preventMultiplayerFeatures) {
            return;
        }
        ServerSelectionList.Entry selectedEntry = serverSelectionList.getSelected();
        if (selectedEntry instanceof ServerSelectionList.OnlineServerEntry) {
            selectButton.setMessage(Component.translatable("selectServer.select"));
        } else if (selectedEntry instanceof AbstractEntry<?, ?> abstractEntry) {
            abstractEntry.updateScreenButtonStates(selectButton, editButton, deleteButton, null);
        }
        organizableplayscreens_buttonCancel.setMessage(serverSelectionList.organizableplayscreens_isRootFolder() ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_BACK);
        organizableplayscreens_buttonMoveEntryBack.active = selectedEntry != null && !(selectedEntry instanceof ServerSelectionList.LANHeader) && !serverSelectionList.organizableplayscreens_isRootFolder();
        for (ServerSelectionList.Entry entry : serverSelectionList.organizableplayscreens_getCurrentEntries()) {
            if (entry instanceof MultiplayerFolderEntry folderEntry) {
                folderEntry.updateButtonStates(selectedEntry);
            }
        }
    }

    /**
     * Saves the folders and servers when the screen is closed.
     */
    @Inject(method = "removed", at = @At(value = "RETURN"))
    private void organizableplayscreens_removed(CallbackInfo ci) {
        if (preventMultiplayerFeatures) {
            return;
        }
        serverSelectionList.organizableplayscreens_saveFile();
    }

    /**
     * Prevents closing the screen if {@link GLFW#GLFW_KEY_ESCAPE} is pressed and {@link com.kevinthegreat.organizableplayscreens.mixin.ServerSelectionListMixin#organizableplayscreens_currentFolder currentFolder} is not the root folder
     *
     * @return whether the screen should close
     */
    @Override
    public boolean shouldCloseOnEsc() {
        return serverSelectionList.organizableplayscreens_isRootFolder();
    }
}
