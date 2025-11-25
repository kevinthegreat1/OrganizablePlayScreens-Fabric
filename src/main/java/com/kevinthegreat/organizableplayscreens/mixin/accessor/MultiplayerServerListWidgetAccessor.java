package com.kevinthegreat.organizableplayscreens.mixin.accessor;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.ThreadPoolExecutor;

@Mixin(MultiplayerServerListWidget.class)
public interface MultiplayerServerListWidgetAccessor {
    @Accessor
    static ThreadPoolExecutor getSERVER_PINGER_THREAD_POOL() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static Text getCANNOT_RESOLVE_TEXT() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static Text getCANNOT_CONNECT_TEXT() {
        throw new UnsupportedOperationException();
    }
}
