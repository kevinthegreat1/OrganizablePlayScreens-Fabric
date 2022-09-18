package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.FolderEntry;
import com.kevinthegreat.organizableplayscreens.MultiplayerScreenAccessor;
import com.kevinthegreat.organizableplayscreens.MultiplayerServerListWidgetAccessor;
import com.kevinthegreat.organizableplayscreens.screen.EditFolderScreen;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen implements MultiplayerScreenAccessor {
    @Shadow
    private ServerInfo selectedEntry;
    @Shadow
    protected MultiplayerServerListWidget serverListWidget;
    @Shadow
    private ButtonWidget buttonJoin;
    @Shadow
    private ButtonWidget buttonEdit;
    @Shadow
    private ButtonWidget buttonDelete;

    @Shadow
    public abstract void select(MultiplayerServerListWidget.Entry entry);

    private MultiplayerServerListWidgetAccessor multiplayerServerListWidgetAccessor;
    private ButtonWidget organizableplayscreens_buttonCancel;
    @Nullable
    private FolderEntry organizableplayscreens_newFolder;

    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V", shift = At.Shift.AFTER))
    private void organizableplayscreens_loadFile(CallbackInfo ci) {
        multiplayerServerListWidgetAccessor = (MultiplayerServerListWidgetAccessor) serverListWidget;
        multiplayerServerListWidgetAccessor.organizableplayscreens_loadFile();
    }

    @Inject(method = "init", at = @At(value = "RETURN"))
    private void organizableplayscreens_addFolderButton(CallbackInfo ci) {
        addDrawableChild(new ButtonWidget(width - 28, 8, 20, 20, Text.of("+"), buttonWidget -> {
            organizableplayscreens_newFolder = new FolderEntry((MultiplayerScreen) (Object) this, multiplayerServerListWidgetAccessor.organizableplayscreens_getCurrentFolder());
            client.setScreen(new EditFolderScreen(this::organizableplayscreens_addFolder, organizableplayscreens_newFolder));
            select(organizableplayscreens_newFolder);
        }));
    }

    @Inject(method = "method_19915", at = @At(value = "RETURN"))
    private void organizableplayscreens_modifyEditButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (serverListWidget.getSelectedOrNull() instanceof FolderEntry folderEntry) {
            client.setScreen(new EditFolderScreen(this::organizableplayscreens_editFolder, folderEntry));
        }
    }

    @Inject(method = "method_19914", at = @At(value = "RETURN"))
    private void organizableplayscreens_modifyDeleteButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (serverListWidget.getSelectedOrNull() instanceof FolderEntry folderEntry) {
            client.setScreen(new ConfirmScreen(this::organizableplayscreens_deleteFolder, Text.translatable("organizableplayscreens:folder.deleteFolderQuestion"), Text.translatable("organizableplayscreens:folder.deleteFolderWarning", folderEntry.getName()), Text.translatable("organizableplayscreens:folder.deleteFolderButton"), ScreenTexts.CANCEL));
        }
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 6))
    private <T extends Element & Drawable & Selectable> T organizableplayscreens_setCancelButton(MultiplayerScreen instance, T element) {
        organizableplayscreens_buttonCancel = addDrawableChild((ButtonWidget) element);
        return element;
    }

    @Inject(method = "method_19912", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_modifyCancelButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (!multiplayerServerListWidgetAccessor.organizableplayscreens_isRootFolder()) {
            multiplayerServerListWidgetAccessor.organizableplayscreens_setCurrentFolderToParent();
            ci.cancel();
        }
    }

    private void organizableplayscreens_addFolder(boolean confirmedAction) {
        if (confirmedAction) {
            multiplayerServerListWidgetAccessor.organizableplayscreens_getCurrentEntries().add(organizableplayscreens_newFolder);
            multiplayerServerListWidgetAccessor.organizableplayscreens_updateAndSave();
            organizableplayscreens_newFolder = null;
        }
        client.setScreen(this);
    }

    private void organizableplayscreens_editFolder(boolean confirmedAction) {
        client.setScreen(this);
    }

    private void organizableplayscreens_deleteFolder(boolean confirmedAction) {
        if (confirmedAction) {
            multiplayerServerListWidgetAccessor.organizableplayscreens_getCurrentEntries().remove(serverListWidget.getSelectedOrNull());
            serverListWidget.setSelected(null);
            multiplayerServerListWidgetAccessor.organizableplayscreens_updateAndSave();
        }
        client.setScreen(this);
    }

    @Inject(method = "addEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V", shift = At.Shift.AFTER))
    private void organizableplayscreens_addServer(boolean confirmedAction, CallbackInfo ci) {
        multiplayerServerListWidgetAccessor.organizableplayscreens_getCurrentEntries().add(serverListWidget.new ServerEntry((MultiplayerScreen) (Object) this, selectedEntry));
        multiplayerServerListWidgetAccessor.organizableplayscreens_updateAndSave();
    }

    @Inject(method = "editEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V", shift = At.Shift.AFTER))
    private void organizableplayscreens_editServer(boolean confirmedAction, CallbackInfo ci) {
        multiplayerServerListWidgetAccessor.organizableplayscreens_getCurrentEntries().set(multiplayerServerListWidgetAccessor.organizableplayscreens_getCurrentEntries().indexOf(serverListWidget.getSelectedOrNull()), serverListWidget.new ServerEntry((MultiplayerScreen) (Object) this, selectedEntry));
        multiplayerServerListWidgetAccessor.organizableplayscreens_updateAndSave();
    }

    @Inject(method = "removeEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setSelected(Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget$Entry;)V"))
    private void organizableplayscreens_removeServer(boolean confirmedAction, CallbackInfo ci) {
        multiplayerServerListWidgetAccessor.organizableplayscreens_getCurrentEntries().remove(serverListWidget.getSelectedOrNull());
        multiplayerServerListWidgetAccessor.organizableplayscreens_updateAndSave();
    }

    @Override
    public void organizableplayscreens_openFolder(FolderEntry folderEntry) {
        multiplayerServerListWidgetAccessor.organizableplayscreens_setCurrentFolder(folderEntry);
    }

    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode == 256 && !shouldCloseOnEsc()) {
            multiplayerServerListWidgetAccessor.organizableplayscreens_setCurrentFolderToParent();
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return multiplayerServerListWidgetAccessor.organizableplayscreens_isRootFolder();
    }

    @Inject(method = "updateButtonActivationStates", at = @At(value = "RETURN"))
    private void organizableplayscreens_updateButtonActivationStates(CallbackInfo ci) {
        MultiplayerServerListWidget.Entry entry = serverListWidget.getSelectedOrNull();
        if (entry instanceof MultiplayerServerListWidget.ServerEntry) {
            buttonJoin.setMessage(Text.translatable("selectServer.select"));
        } else if (entry instanceof FolderEntry) {
            buttonJoin.setMessage(Text.translatable("organizableplayscreens:selectServer.openFolder"));
            buttonJoin.active = true;
            buttonEdit.active = true;
            buttonDelete.active = true;
        }
        organizableplayscreens_buttonCancel.setMessage(multiplayerServerListWidgetAccessor.organizableplayscreens_isRootFolder() ? ScreenTexts.CANCEL : ScreenTexts.BACK);
    }
}
