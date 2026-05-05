package com.kevinthegreat.organizableplayscreens.mixin.accessor;

import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.ThreadPoolExecutor;

@Mixin(ServerSelectionList.class)
public interface ServerSelectionListAccessor {
    @Accessor
    static ThreadPoolExecutor getTHREAD_POOL() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static Component getCANT_RESOLVE_TEXT() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static Component getCANT_CONNECT_TEXT() {
        throw new UnsupportedOperationException();
    }
}
