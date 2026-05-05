package com.kevinthegreat.organizableplayscreens.mixin.accessor;

import net.minecraft.world.level.storage.LevelVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelVersion.class)
public interface LevelVersionInvoker {
    @SuppressWarnings("unused")
    @Invoker("<init>")
    static LevelVersion create(int levelFormatVersion, long lastPlayed, String versionName, int versionId, String series, boolean stable) {
        throw new IllegalStateException("Mixin invoker failed to apply");
    }
}

