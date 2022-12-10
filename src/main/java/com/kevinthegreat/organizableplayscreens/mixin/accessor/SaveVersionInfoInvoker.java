package com.kevinthegreat.organizableplayscreens.mixin.accessor;

import net.minecraft.world.level.storage.SaveVersionInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SaveVersionInfo.class)
public interface SaveVersionInfoInvoker {
    @SuppressWarnings("unused")
    @Invoker("<init>")
    static SaveVersionInfo create(int levelFormatVersion, long lastPlayed, String versionName, int versionId, String series, boolean stable) {
        throw new IllegalStateException("Mixin invoker failed to apply");
    }
}

