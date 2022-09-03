package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.FolderEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(MultiplayerServerListWidget.class)
public abstract class MultiplayerServerListWidgetMixin extends AlwaysSelectedEntryListWidget<MultiplayerServerListWidget.Entry> {
    @Shadow
    @Final
    private MultiplayerServerListWidget.Entry scanningEntry;
    @Shadow
    @Final
    private List<MultiplayerServerListWidget.LanServerEntry> lanServers;
    private final List<MultiplayerServerListWidget.Entry> entries = new ArrayList<>();
    @Nullable
    public FolderEntry currentFolder;

    public MultiplayerServerListWidgetMixin(MinecraftClient minecraftClient, int i, int j, int k, int l, int m) {
        super(minecraftClient, i, j, k, l, m);
    }

    @Inject(method = "updateEntries", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_updateEntries(CallbackInfo ci) {
        clearEntries();
        children().addAll(currentFolder == null ? entries : currentFolder.getEntries());
        children().add(scanningEntry);
        children().addAll(lanServers);
        ci.cancel();
    }
}
