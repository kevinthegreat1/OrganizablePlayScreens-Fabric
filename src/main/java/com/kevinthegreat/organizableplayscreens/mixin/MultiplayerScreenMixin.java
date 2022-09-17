package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.FolderEntry;
import com.kevinthegreat.organizableplayscreens.MultiplayerScreenAccessor;
import com.kevinthegreat.organizableplayscreens.MultiplayerServerListWidgetAccessor;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
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
    private MultiplayerServerListWidgetAccessor multiplayerServerListWidgetAccessor;
    private ButtonWidget organizableplayscreens_buttonCancel;

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
        addDrawableChild(new ButtonWidget(width - 28, 8, 20, 20, Text.of("+"), buttonWidget -> organizableplayscreens_addFolder()));
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

    public void organizableplayscreens_addFolder() {
        multiplayerServerListWidgetAccessor.organizableplayscreens_getCurrentEntries().add(new FolderEntry((MultiplayerScreen) (Object) this, multiplayerServerListWidgetAccessor.organizableplayscreens_getCurrentFolder(), "New Folder"));
        multiplayerServerListWidgetAccessor.organizableplayscreens_updateAndSave();
    }

    @Redirect(method = "addEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V"))
    private void organizableplayscreens_addServer(MultiplayerServerListWidget instance, ServerList servers) {
        multiplayerServerListWidgetAccessor.organizableplayscreens_getCurrentEntries().add(serverListWidget.new ServerEntry((MultiplayerScreen) (Object) this, selectedEntry));
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
