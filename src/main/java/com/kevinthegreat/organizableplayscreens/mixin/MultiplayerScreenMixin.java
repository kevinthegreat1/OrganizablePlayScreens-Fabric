package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.compatibility.Compatibility;
import com.kevinthegreat.organizableplayscreens.gui.AbstractEntry;
import com.kevinthegreat.organizableplayscreens.gui.AbstractMultiplayerEntry;
import com.kevinthegreat.organizableplayscreens.gui.MultiplayerFolderEntry;
import com.kevinthegreat.organizableplayscreens.gui.screen.MultiplayerEditEntryScreen;
import com.kevinthegreat.organizableplayscreens.gui.screen.OrganizablePlayScreensOptionsScreen;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("JavadocReference")
@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    @Shadow
    @Final
    private Screen parent;
    @Shadow
    protected MultiplayerServerListWidget serverListWidget;
    @Shadow
    private ButtonWidget buttonJoin;
    @Shadow
    private ButtonWidget buttonEdit;
    @Shadow
    private ButtonWidget buttonDelete;
    @Shadow
    private ServerInfo selectedEntry;

    /**
     * This is the vanilla cancel button because this is not saved in a vanilla field.
     */
    @Unique
    private ButtonWidget organizableplayscreens_buttonCancel;
    /**
     * This button sets {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent folder if there is one, otherwise to the parent screen.
     */
    @Unique
    private ButtonWidget organizableplayscreens_buttonBack;
    /**
     * This button moves the selected entry to the parent folder.
     */
    @Unique
    private ButtonWidget organizableplayscreens_buttonMoveEntryBack;
    /**
     * This button opens a screen to create a new folder and stores it in {@link #organizableplayscreens_newEntry newEntry}.
     */
    @Unique
    private ButtonWidget organizableplayscreens_buttonNewEntry;
    /**
     * This button opens the options screen.
     */
    @Unique
    private ButtonWidget organizableplayscreens_buttonOptions;
    /**
     * The path of the {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder}.
     * <p>
     * Only used for display. In the form of '{@code folder > child folder}'. Empty in the root folder.
     */
    @Unique
    private TextWidget organizableplayscreens_pathWidget;
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

    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    /**
     * Adds the path of the {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to the header of the screen.
     *
     * @see #organizableplayscreens_pathWidget pathWidget
     */
    @WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ThreePartsLayoutWidget;addHeader(Lnet/minecraft/text/Text;Lnet/minecraft/client/font/TextRenderer;)V"))
    private void organizableplayscreens_modifyHeader(ThreePartsLayoutWidget layout, Text text, TextRenderer textRenderer, Operation<Void> original) {
        if (Compatibility.essential_preventMultiplayerFeatures()) {
            preventMultiplayerFeatures = true;
            return;
        }

        DirectionalLayoutWidget headerLayout = layout.addHeader(DirectionalLayoutWidget.vertical().spacing(4));
        headerLayout.getMainPositioner().alignHorizontalCenter();
        if (organizableplayscreens_pathWidget == null) organizableplayscreens_pathWidget = new TextWidget(Text.empty(), textRenderer).setTextColor(0xFFA0A0A0);
        headerLayout.add(organizableplayscreens_pathWidget);
        headerLayout.add(new TextWidget(text, textRenderer));
    }

    /**
     * Loads and displays folders and servers from {@code organizable_servers.dat} and adds 'back', 'move entry back', 'new folder', and 'options' buttons to the screen.
     * <p>
     * The 'back' button sets {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one, otherwise to the parent screen.
     * The 'move entry back' button moves the selected entry to the parent folder.
     * The 'new folder' button opens a screen to create a new folder and stores it in {@link #organizableplayscreens_newEntry newFolder}.
     */
    @WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V"))
    private void organizableplayscreens_loadFileAndAddButtons(MultiplayerServerListWidget serverListWidget, ServerList servers, Operation<Void> original) {
        if (Compatibility.essential_preventMultiplayerFeatures()) {
            preventMultiplayerFeatures = true;
            return;
        }

        serverListWidget.organizableplayscreens_setPathWidget(organizableplayscreens_pathWidget);
        original.call(serverListWidget, servers);
        serverListWidget.organizableplayscreens_loadFile();

        OrganizablePlayScreensOptions options = OrganizablePlayScreens.getInstance().options;

        organizableplayscreens_buttonBack = addDrawableChild(ButtonWidget.builder(Text.of("←"), buttonWidget -> {
            if (!serverListWidget.organizableplayscreens_setCurrentFolderToParent()) {
                client.setScreen(parent);
            }
        }).dimensions(options.backButtonX.getValue(), options.backButtonY.getValue(), 20, 20).build());
        organizableplayscreens_buttonMoveEntryBack = addDrawableChild(ButtonWidget.builder(Text.of("←+"), buttonWidget -> {
            if (!serverListWidget.organizableplayscreens_isRootFolder()) {
                MultiplayerServerListWidget.Entry entry = serverListWidget.getSelectedOrNull();
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
        }).dimensions(options.moveEntryBackButtonX.getValue(), options.moveEntryBackButtonY.getValue(), 20, 20).tooltip(OrganizablePlayScreens.MOVE_ENTRY_BACK_TOOLTIP).build());
        organizableplayscreens_buttonNewEntry = addDrawableChild(ButtonWidget.builder(Text.of("+"), buttonWidget -> client.setScreen(new MultiplayerEditEntryScreen(this, this::organizableplayscreens_addEntry, type -> {
            MultiplayerFolderEntry folder = serverListWidget.organizableplayscreens_getCurrentFolder();
            return organizableplayscreens_newEntry = type.multiplayerEntry((MultiplayerScreen) (Object) this, folder);
        }))).dimensions(options.getValue(options.newFolderButtonX), options.newFolderButtonY.getValue(), 20, 20).build());
        organizableplayscreens_buttonOptions = addDrawableChild(new LegacyTexturedButtonWidget(options.getValue(options.optionsButtonX), options.optionsButtonY.getValue(), 20, 20, 0, 0, 20, OrganizablePlayScreens.OPTIONS_BUTTON_TEXTURE, 32, 64, buttonWidget -> client.setScreen(new OrganizablePlayScreensOptionsScreen(this)), Text.translatable("organizableplayscreens:options.optionsButton")));
    }

    /**
     * Updates the positions of the added buttons according to the options. Called when the screen is reinitialized or resized.
     */
    @Inject(method = "refreshWidgetPositions", at = @At(value = "RETURN"))
    private void organizableplayscreens_refreshWidgetPositions(CallbackInfo ci) {
        OrganizablePlayScreensOptions options = OrganizablePlayScreens.getInstance().options;
        if (organizableplayscreens_buttonBack != null) organizableplayscreens_buttonBack.setPosition(options.backButtonX.getValue(), options.backButtonY.getValue());
        if (organizableplayscreens_buttonMoveEntryBack != null) organizableplayscreens_buttonMoveEntryBack.setPosition(options.moveEntryBackButtonX.getValue(), options.moveEntryBackButtonY.getValue());
        if (organizableplayscreens_buttonNewEntry != null) organizableplayscreens_buttonNewEntry.setPosition(options.getValue(options.newFolderButtonX), options.newFolderButtonY.getValue());
        if (organizableplayscreens_buttonOptions != null) organizableplayscreens_buttonOptions.setPosition(options.getValue(options.optionsButtonX), options.optionsButtonY.getValue());
    }

    /**
     * Modifies the 'edit' button to work with folders.
     */
    @Inject(method = "method_19915", at = @At(value = "RETURN"))
    private void organizableplayscreens_modifyEditButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (serverListWidget.getSelectedOrNull() instanceof AbstractMultiplayerEntry entry) {
            client.setScreen(new MultiplayerEditEntryScreen(this, this::organizableplayscreens_editEntry, entry));
        }
    }

    /**
     * Modifies the 'delete' button to work with folders.
     */
    @Inject(method = "method_19914", at = @At(value = "RETURN"))
    private void organizableplayscreens_modifyDeleteButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (serverListWidget.getSelectedOrNull() instanceof AbstractMultiplayerEntry entry) {
            boolean isFolder = entry instanceof MultiplayerFolderEntry;
            client.setScreen(new ConfirmScreen(this::organizableplayscreens_deleteEntry, Text.translatable("organizableplayscreens:entry.deleteEntryQuestion", entry.getType().text().getString()), Text.translatable(isFolder ? "organizableplayscreens:folder.deleteMultiplayerFolderWarning" : "organizableplayscreens:entry.deleteEntryWarning", entry.getName()), Text.translatable("selectServer.deleteButton"), ScreenTexts.CANCEL));
        }
    }

    /**
     * Saves the 'cancel' button instance in a field so its text can be changed.
     */
    @ModifyExpressionValue(method = "init", slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/screen/ScreenTexts;BACK:Lnet/minecraft/text/Text;", opcode = Opcodes.GETSTATIC)), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;build()Lnet/minecraft/client/gui/widget/ButtonWidget;", ordinal = 0))
    private ButtonWidget organizableplayscreens_setCancelButton(ButtonWidget buttonCancel) {
        return organizableplayscreens_buttonCancel = buttonCancel;
    }

    /**
     * Modifies the 'cancel' button to set {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one and prevent closing the screen.
     */
    @Inject(method = "method_19912", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_modifyCancelButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (serverListWidget.organizableplayscreens_setCurrentFolderToParent()) {
            ci.cancel();
        }
    }

    /**
     * Adds the non-server entry stored in {@link #organizableplayscreens_newEntry newEntry} to {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} and sets the screen back to this.
     */
    @Unique
    private void organizableplayscreens_addEntry(boolean confirmedAction) {
        if (confirmedAction) {
            serverListWidget.organizableplayscreens_getCurrentEntries().add(organizableplayscreens_newEntry);
            serverListWidget.organizableplayscreens_updateAndSave();
            serverListWidget.setSelected(organizableplayscreens_newEntry);
            organizableplayscreens_newEntry = null;
        }
        client.setScreen(this);
    }

    /**
     * Sets the screen back to this after finishing editing the selected non-server entry.
     */
    @Unique
    private void organizableplayscreens_editEntry(boolean confirmedAction) {
        client.setScreen(this);
    }

    /**
     * Deletes the selected non-server entry, updates the displayed entries, and sets the screen back to this.
     */
    @Unique
    private void organizableplayscreens_deleteEntry(boolean confirmedAction) {
        if (confirmedAction) {
            serverListWidget.organizableplayscreens_getCurrentEntries().remove(serverListWidget.getSelectedOrNull());
            serverListWidget.setSelected(null);
            serverListWidget.organizableplayscreens_updateAndSave();
        }
        client.setScreen(this);
    }

    /**
     * Adds the server in {@link MultiplayerScreen#selectedEntry selectedEntry} to {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder}.
     */
    @Inject(method = "addEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V", shift = At.Shift.AFTER))
    private void organizableplayscreens_addServer(boolean confirmedAction, CallbackInfo ci) {
        serverListWidget.organizableplayscreens_getCurrentEntries().add(MultiplayerServerListWidgetMixin.ServerEntryAccessor.create(serverListWidget, (MultiplayerScreen) (Object) this, selectedEntry));
        serverListWidget.organizableplayscreens_updateAndSave();
    }

    /**
     * Edits the selected server to match {@link MultiplayerScreen#selectedEntry selectedEntry} in {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder}.
     */
    @Inject(method = "editEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V"))
    private void organizableplayscreens_editServer(boolean confirmedAction, CallbackInfo ci) {
        serverListWidget.organizableplayscreens_getCurrentEntries().set(serverListWidget.organizableplayscreens_getCurrentEntries().indexOf(serverListWidget.getSelectedOrNull()), MultiplayerServerListWidgetMixin.ServerEntryAccessor.create(serverListWidget, (MultiplayerScreen) (Object) this, selectedEntry));
        serverListWidget.organizableplayscreens_updateAndSave();
    }

    /**
     * Removes the selected server from {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder}.
     */
    @Inject(method = "removeEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setSelected(Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget$Entry;)V"))
    private void organizableplayscreens_removeServer(boolean confirmedAction, CallbackInfo ci) {
        serverListWidget.organizableplayscreens_getCurrentEntries().remove(serverListWidget.getSelectedOrNull());
        serverListWidget.organizableplayscreens_updateAndSave();
    }

    /**
     * Opens the selected entry if it is a folder.
     */
    @Inject(method = "connect", at = @At(value = "RETURN"))
    private void organizableplayscreens_openFolder(CallbackInfo ci) {
        if (serverListWidget.getSelectedOrNull() instanceof AbstractMultiplayerEntry entry) {
            entry.entrySelectionConfirmed(serverListWidget);
        }
    }

    /**
     * Sets {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one and prevents closing the screen if {@link GLFW#GLFW_KEY_ESCAPE} is pressed.
     */
    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_keyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE && !shouldCloseOnEsc() && serverListWidget.organizableplayscreens_setCurrentFolderToParent() && !Compatibility.essential_preventMultiplayerFeatures()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Updates the activation states of buttons. Called at the end of {@link #init()} and every time an entry is selected.
     */
    @Inject(method = "updateButtonActivationStates", at = @At(value = "RETURN"))
    private void organizableplayscreens_updateButtonActivationStates(CallbackInfo ci) {
        if (preventMultiplayerFeatures) {
            return;
        }
        MultiplayerServerListWidget.Entry selectedEntry = serverListWidget.getSelectedOrNull();
        if (selectedEntry instanceof MultiplayerServerListWidget.ServerEntry) {
            buttonJoin.setMessage(Text.translatable("selectServer.select"));
        } else if (selectedEntry instanceof AbstractEntry<?, ?> abstractEntry) {
            abstractEntry.updateScreenButtonStates(buttonJoin, buttonEdit, buttonDelete, null);
        }
        organizableplayscreens_buttonCancel.setMessage(serverListWidget.organizableplayscreens_isRootFolder() ? ScreenTexts.CANCEL : ScreenTexts.BACK);
        organizableplayscreens_buttonMoveEntryBack.active = selectedEntry != null && !(selectedEntry instanceof MultiplayerServerListWidget.ScanningEntry) && !serverListWidget.organizableplayscreens_isRootFolder();
        for (MultiplayerServerListWidget.Entry entry : serverListWidget.organizableplayscreens_getCurrentEntries()) {
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
        serverListWidget.organizableplayscreens_saveFile();
    }

    /**
     * Prevents closing the screen if {@link GLFW#GLFW_KEY_ESCAPE} is pressed and {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} is not the root folder
     *
     * @return whether the screen should close
     */
    @Override
    public boolean shouldCloseOnEsc() {
        return serverListWidget.organizableplayscreens_isRootFolder();
    }
}
