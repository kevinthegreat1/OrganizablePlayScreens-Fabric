package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.FolderEntry;
import com.kevinthegreat.organizableplayscreens.MultiplayerScreenAccessor;
import com.kevinthegreat.organizableplayscreens.MultiplayerServerListWidgetAccessor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen implements MultiplayerScreenAccessor {
    @Shadow
    private ServerInfo selectedEntry;
    @Shadow
    protected MultiplayerServerListWidget serverListWidget;
    private MultiplayerServerListWidgetAccessor multiplayerServerListWidgetAccessor;

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

    @Inject(method = "method_19912", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_modifyCancelButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (!multiplayerServerListWidgetAccessor.organizableplayscreens_isRootFolder()) {
            multiplayerServerListWidgetAccessor.organizableplayscreens_setCurrentFolder(multiplayerServerListWidgetAccessor.organizableplayscreens_getCurrentFolder().getParent());
            serverListWidget.updateEntries();
            ci.cancel();
        }
    }

    public void organizableplayscreens_addFolder() {
        multiplayerServerListWidgetAccessor.organizableplayscreens_getCurrentEntries().add(new FolderEntry((MultiplayerScreen) (Object) this, multiplayerServerListWidgetAccessor.organizableplayscreens_getCurrentFolder(), "New Folder"));
        multiplayerServerListWidgetAccessor.organizableplayscreens_saveFile();
        serverListWidget.updateEntries();
    }

    @Redirect(method = "addEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V"))
    private void organizableplayscreens_addServer(MultiplayerServerListWidget instance, ServerList servers) {
        multiplayerServerListWidgetAccessor.organizableplayscreens_getCurrentEntries().add(serverListWidget.new ServerEntry((MultiplayerScreen) (Object) this, selectedEntry));
        multiplayerServerListWidgetAccessor.organizableplayscreens_saveFile();
        serverListWidget.updateEntries();
    }

    @Override
    public void organizableplayscreens_openFolder(FolderEntry folderEntry) {
        multiplayerServerListWidgetAccessor.organizableplayscreens_setCurrentFolder(folderEntry);
        serverListWidget.updateEntries();
    }
}
