package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.compatibility.Compatibility;
import com.kevinthegreat.organizableplayscreens.gui.MultiplayerFolderEntry;
import com.kevinthegreat.organizableplayscreens.gui.MultiplayerServerListWidgetAccessor;
import com.kevinthegreat.organizableplayscreens.gui.screen.EditFolderScreen;
import com.kevinthegreat.organizableplayscreens.gui.screen.OrganizablePlayScreensOptionsScreen;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Shadow
    public abstract void select(MultiplayerServerListWidget.Entry entry);

    /**
     * An accessor to access methods in {@link MultiplayerServerListWidgetMixin}
     */
    public MultiplayerServerListWidgetAccessor serverListWidgetAccessor;
    /**
     * This is the vanilla cancel button because this is not saved in a vanilla field.
     */
    private ButtonWidget organizableplayscreens_buttonCancel;
    /**
     * This button moves the selected entry to the parent folder.
     */
    private ButtonWidget organizableplayscreens_buttonMoveEntryBack;
    /**
     * A folder entry to store the folder that is currently being created.
     */
    @Nullable
    private MultiplayerFolderEntry organizableplayscreens_newFolder;

    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    /**
     * Sets the accessor to prevent having to cast it every time and loads and displays folders and servers from {@code organizable_servers.dat}.
     */
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V", shift = At.Shift.AFTER))
    private void organizableplayscreens_loadFile(CallbackInfo ci) {
        serverListWidgetAccessor = (MultiplayerServerListWidgetAccessor) serverListWidget;
        serverListWidgetAccessor.organizableplayscreens_loadFile();
    }

    /**
     * Adds 'back', 'move entry back', 'new folder', and 'options' buttons to the screen.
     * <p>
     * The 'back' button sets {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one, otherwise to the parent screen.
     * The 'move entry back' button moves the selected entry to the parent folder.
     * The 'new folder' button opens a screen to create a new folder and stores it in {@link #organizableplayscreens_newFolder newFolder}.
     */
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;updateButtonActivationStates()V"))
    private void organizableplayscreens_addButtons(CallbackInfo ci) {
        OrganizablePlayScreensOptions options = OrganizablePlayScreens.getInstance().options;
        addDrawableChild(ButtonWidget.builder(Text.of("←"), buttonWidget -> {
            if (!serverListWidgetAccessor.organizableplayscreens_setCurrentFolderToParent()) {
                client.setScreen(parent);
            }
        }).dimensions(options.backButtonX.getValue(), options.backButtonY.getValue(), 20, 20).build());
        organizableplayscreens_buttonMoveEntryBack = addDrawableChild(ButtonWidget.builder(Text.of("←+"), buttonWidget -> {
            if (!serverListWidgetAccessor.organizableplayscreens_isRootFolder()) {
                MultiplayerServerListWidget.Entry entry = serverListWidget.getSelectedOrNull();
                if (entry != null) {
                    MultiplayerFolderEntry parentFolder = serverListWidgetAccessor.organizableplayscreens_getCurrentFolder().getParent();
                    if (entry instanceof MultiplayerFolderEntry folderEntry) {
                        folderEntry.setParent(parentFolder);
                    }
                    parentFolder.getEntries().add(entry);
                    serverListWidgetAccessor.organizableplayscreens_getCurrentEntries().remove(entry);
                    serverListWidgetAccessor.organizableplayscreens_updateAndSave();
                }
            }
        }).dimensions(options.moveEntryBackButtonX.getValue(), options.moveEntryBackButtonY.getValue(), 20, 20).tooltip(OrganizablePlayScreens.MOVE_ENTRY_BACK_TOOLTIP).build());
        addDrawableChild(ButtonWidget.builder(Text.of("+"), buttonWidget -> {
            organizableplayscreens_newFolder = new MultiplayerFolderEntry((MultiplayerScreen) (Object) this, serverListWidgetAccessor.organizableplayscreens_getCurrentFolder());
            client.setScreen(new EditFolderScreen(this, this::organizableplayscreens_addFolder, organizableplayscreens_newFolder, true));
            select(organizableplayscreens_newFolder);
        }).dimensions(options.getValue(options.newFolderButtonX), options.newFolderButtonY.getValue(), 20, 20).build());
        addDrawableChild(new TexturedButtonWidget(options.getValue(options.optionsButtonX), options.optionsButtonY.getValue(), 20, 20, 0, 0, 20, OrganizablePlayScreens.OPTIONS_BUTTON_TEXTURE, 32, 64, buttonWidget -> client.setScreen(new OrganizablePlayScreensOptionsScreen(this))));
    }

    /**
     * Modifies the 'edit' button to work with folders.
     */
    @Inject(method = "method_19915", at = @At(value = "RETURN"))
    private void organizableplayscreens_modifyEditButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (serverListWidget.getSelectedOrNull() instanceof MultiplayerFolderEntry folderEntry) {
            client.setScreen(new EditFolderScreen(this, this::organizableplayscreens_editFolder, folderEntry, false));
        }
    }

    /**
     * Modifies the 'delete' button to work with folders.
     */
    @Inject(method = "method_19914", at = @At(value = "RETURN"))
    private void organizableplayscreens_modifyDeleteButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (serverListWidget.getSelectedOrNull() instanceof MultiplayerFolderEntry folderEntry) {
            client.setScreen(new ConfirmScreen(this::organizableplayscreens_deleteFolder, Text.translatable("organizableplayscreens:folder.deleteFolderQuestion"), Text.translatable("organizableplayscreens:folder.deleteMultiplayerFolderWarning", folderEntry.getName()), Text.translatable("selectServer.deleteButton"), ScreenTexts.CANCEL));
        }
    }

    /**
     * Saves the 'cancel' button instance in a field so its text can be changed.
     */
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 6))
    private <T extends Element & Drawable & Selectable> T organizableplayscreens_setCancelButton(MultiplayerScreen instance, T element) {
        organizableplayscreens_buttonCancel = addDrawableChild((ButtonWidget) element);
        return element;
    }

    /**
     * Modifies the 'cancel' button to set {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one and prevent closing the screen.
     */
    @Inject(method = "method_19912", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_modifyCancelButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (serverListWidgetAccessor.organizableplayscreens_setCurrentFolderToParent()) {
            ci.cancel();
        }
    }

    /**
     * Adds the folder stored in {@link #organizableplayscreens_newFolder newFolder} to {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} and sets the screen back to this.
     */
    private void organizableplayscreens_addFolder(boolean confirmedAction) {
        if (confirmedAction) {
            serverListWidgetAccessor.organizableplayscreens_getCurrentEntries().add(organizableplayscreens_newFolder);
            serverListWidgetAccessor.organizableplayscreens_updateAndSave();
            organizableplayscreens_newFolder = null;
        }
        client.setScreen(this);
    }

    /**
     * Sets the screen back to this after finishing editing the selected folder.
     */
    private void organizableplayscreens_editFolder(boolean confirmedAction) {
        client.setScreen(this);
    }

    /**
     * Deletes the selected folder, updates the displayed entries, and sets the screen back to this.
     */
    private void organizableplayscreens_deleteFolder(boolean confirmedAction) {
        if (confirmedAction) {
            serverListWidgetAccessor.organizableplayscreens_getCurrentEntries().remove(serverListWidget.getSelectedOrNull());
            select(null);
            serverListWidgetAccessor.organizableplayscreens_updateAndSave();
        }
        client.setScreen(this);
    }

    /**
     * Adds the server in {@link MultiplayerScreen#selectedEntry selectedEntry} to {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder}.
     */
    @Inject(method = "addEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V", shift = At.Shift.AFTER))
    private void organizableplayscreens_addServer(boolean confirmedAction, CallbackInfo ci) {
        serverListWidgetAccessor.organizableplayscreens_getCurrentEntries().add(MultiplayerServerListWidgetMixin.ServerEntryInvoker.create(serverListWidget, (MultiplayerScreen) (Object) this, selectedEntry));
        serverListWidgetAccessor.organizableplayscreens_updateAndSave();
    }

    /**
     * Edits the selected server to match {@link MultiplayerScreen#selectedEntry selectedEntry} in {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder}.
     */
    @Inject(method = "editEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V", shift = At.Shift.AFTER))
    private void organizableplayscreens_editServer(boolean confirmedAction, CallbackInfo ci) {
        serverListWidgetAccessor.organizableplayscreens_getCurrentEntries().set(serverListWidgetAccessor.organizableplayscreens_getCurrentEntries().indexOf(serverListWidget.getSelectedOrNull()), MultiplayerServerListWidgetMixin.ServerEntryInvoker.create(serverListWidget, (MultiplayerScreen) (Object) this, selectedEntry));
        serverListWidgetAccessor.organizableplayscreens_updateAndSave();
    }

    /**
     * Removes the selected server from {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder}.
     */
    @Inject(method = "removeEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setSelected(Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget$Entry;)V"))
    private void organizableplayscreens_removeServer(boolean confirmedAction, CallbackInfo ci) {
        serverListWidgetAccessor.organizableplayscreens_getCurrentEntries().remove(serverListWidget.getSelectedOrNull());
        serverListWidgetAccessor.organizableplayscreens_updateAndSave();
    }

    /**
     * Opens the selected entry if it is a folder.
     */
    @Inject(method = "connect()V", at = @At(value = "RETURN"))
    private void organizableplayscreens_openFolder(CallbackInfo ci) {
        if (serverListWidget.getSelectedOrNull() instanceof MultiplayerFolderEntry folderEntry) {
            serverListWidgetAccessor.organizableplayscreens_setCurrentFolder(folderEntry);
        }
    }

    /**
     * Sets {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} to its parent if there is one and prevents closing the screen if {@link GLFW#GLFW_KEY_ESCAPE} is pressed.
     *
     * @param keyCode the key code that was pressed
     */
    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !shouldCloseOnEsc() && serverListWidgetAccessor.organizableplayscreens_setCurrentFolderToParent() && !Compatibility.essential_preventMultiplayerFeatures()) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    /**
     * Renders the path of {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder}.
     *
     * @see MultiplayerServerListWidgetMixin#organizableplayscreens_currentPath currentPath
     */
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawCenteredTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V", shift = At.Shift.AFTER))
    private void organizableplayscreens_renderPath(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.drawCenteredTextWithShadow(textRenderer, serverListWidgetAccessor.organizableplayscreens_getCurrentPath(), width / 2, 6, 0xa0a0a0);
    }

    /**
     * Updates the activation states of buttons. Called at the end of {@link #init()} and every time an entry is selected.
     */
    @Inject(method = "updateButtonActivationStates", at = @At(value = "RETURN"))
    private void organizableplayscreens_updateButtonActivationStates(CallbackInfo ci) {
        if (Compatibility.essential_preventMultiplayerFeatures()) {
            return;
        }
        MultiplayerServerListWidget.Entry selectedEntry = serverListWidget.getSelectedOrNull();
        if (selectedEntry instanceof MultiplayerServerListWidget.ServerEntry) {
            buttonJoin.setMessage(Text.translatable("selectServer.select"));
        } else if (selectedEntry instanceof MultiplayerFolderEntry) {
            buttonJoin.setMessage(Text.translatable("organizableplayscreens:folder.openFolder"));
            buttonJoin.active = true;
            buttonEdit.active = true;
            buttonDelete.active = true;
        }
        organizableplayscreens_buttonCancel.setMessage(serverListWidgetAccessor.organizableplayscreens_isRootFolder() ? ScreenTexts.CANCEL : ScreenTexts.BACK);
        organizableplayscreens_buttonMoveEntryBack.active = selectedEntry != null && !serverListWidgetAccessor.organizableplayscreens_isRootFolder();
        for (MultiplayerServerListWidget.Entry entry : serverListWidgetAccessor.organizableplayscreens_getCurrentEntries()) {
            if (entry instanceof MultiplayerFolderEntry folderEntry) {
                folderEntry.updateButtonStates();
            }
        }
    }

    /**
     * Saves the folders and servers when the screen is closed.
     */
    @Override
    public void removed() {
        serverListWidgetAccessor.organizableplayscreens_saveFile();
        super.removed();
    }

    /**
     * Prevents closing the screen if {@link GLFW#GLFW_KEY_ESCAPE} is pressed and {@link com.kevinthegreat.organizableplayscreens.mixin.MultiplayerServerListWidgetMixin#organizableplayscreens_currentFolder currentFolder} is not the root folder
     *
     * @return whether the screen should close
     */
    @Override
    public boolean shouldCloseOnEsc() {
        return serverListWidgetAccessor.organizableplayscreens_isRootFolder();
    }
}
