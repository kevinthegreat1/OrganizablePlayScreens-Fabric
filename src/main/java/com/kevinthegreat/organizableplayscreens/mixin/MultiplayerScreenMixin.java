package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.EntriesAccessor;
import com.kevinthegreat.organizableplayscreens.FolderEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    @Shadow
    private ServerInfo selectedEntry;
    @Shadow
    protected MultiplayerServerListWidget serverListWidget;

    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V", shift = At.Shift.AFTER))
    private void organizableplayscreens_loadFile(CallbackInfo ci) {
        ((EntriesAccessor) serverListWidget).organizableplayscreens_loadFile();
    }

    @Inject(method = "init", at = @At(value = "RETURN"))
    private void organizableplayscreens_addFolderButton(CallbackInfo ci) {
        addDrawableChild(new ButtonWidget(width - 28, 8, 20, 20, Text.of("+"), buttonWidget -> organizableplayscreens_addFolder()));
    }

    public void organizableplayscreens_addFolder() {
        ((EntriesAccessor) serverListWidget).organizableplayscreens_getCurrentEntries().add(new FolderEntry("New Folder"));
        ((EntriesAccessor) serverListWidget).organizableplayscreens_saveFile();
        serverListWidget.updateEntries();
    }

    @Redirect(method = "addEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V"))
    private void organizableplayscreens_addServer(MultiplayerServerListWidget instance, ServerList servers) {
        ((EntriesAccessor) serverListWidget).organizableplayscreens_getCurrentEntries().add(serverListWidget.new ServerEntry((MultiplayerScreen) (Object) this, selectedEntry));
        ((EntriesAccessor) serverListWidget).organizableplayscreens_saveFile();
        serverListWidget.updateEntries();
    }
}
