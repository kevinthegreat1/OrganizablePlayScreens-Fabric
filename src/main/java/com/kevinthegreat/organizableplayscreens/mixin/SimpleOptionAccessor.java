package com.kevinthegreat.organizableplayscreens.mixin;

import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(SimpleOption.class)
public interface SimpleOptionAccessor {
    @Accessor
    <T> T getDefaultValue();

    @Accessor
    <T> Function<T, Text> getTextGetter();
}
