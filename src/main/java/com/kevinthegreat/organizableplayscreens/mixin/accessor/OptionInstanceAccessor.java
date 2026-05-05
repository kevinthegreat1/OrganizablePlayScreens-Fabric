package com.kevinthegreat.organizableplayscreens.mixin.accessor;

import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(OptionInstance.class)
public interface OptionInstanceAccessor {
    @Accessor
    <T> T getInitialValue();

    @Accessor
    <T> Function<T, Component> getToString();
}
