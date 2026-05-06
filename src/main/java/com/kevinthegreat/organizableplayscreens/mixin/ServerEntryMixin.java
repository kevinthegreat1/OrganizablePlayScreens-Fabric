package com.kevinthegreat.organizableplayscreens.mixin;

import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public abstract class ServerEntryMixin {
    @Shadow
    @Final
    ServerSelectionList this$0;

    /**
     * Gets the size of our entry list instead of the vanilla one.
     */
    @Redirect(method = {"extractContent", "keyPressed", "mouseClicked"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ServerList;size()I"))
    private int organizableplayscreens_size(ServerList instance) {
        return this$0.organizableplayscreens_getCurrentEntries().size();
    }

    /**
     * Swaps entries with our entry list instead of the vanilla one.
     */
    @Inject(method = "swap", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_swapEntries(int i, int j, CallbackInfo ci) {
        this$0.organizableplayscreens_swapEntries(i, j);
        ci.cancel();
    }
}