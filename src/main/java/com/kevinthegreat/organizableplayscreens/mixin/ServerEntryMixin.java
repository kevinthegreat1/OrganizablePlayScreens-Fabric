package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.gui.MultiplayerServerListWidgetAccessor;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.option.ServerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class ServerEntryMixin {
    @Shadow
    @Final
    MultiplayerServerListWidget field_19117;

    /**
     * Gets the size of our entry list instead of the vanilla one.
     */
    @Redirect(method = {"render", "keyPressed", "mouseClicked"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/ServerList;size()I"))
    private int organizableplayscreens_size(ServerList instance) {
        return ((MultiplayerServerListWidgetAccessor) field_19117).organizableplayscreens_getCurrentEntries().size();
    }

    /**
     * Swaps entries with our entry list instead of the vanilla one.
     */
    @Inject(method = "swapEntries", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_swapEntries(int i, int j, CallbackInfo ci) {
        ((MultiplayerServerListWidgetAccessor) field_19117).organizableplayscreens_swapEntries(i, j);
        ci.cancel();
    }
}