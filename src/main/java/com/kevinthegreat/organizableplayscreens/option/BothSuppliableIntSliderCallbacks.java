package com.kevinthegreat.organizableplayscreens.option;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntSupplier;

public record BothSuppliableIntSliderCallbacks(IntSupplier minSupplier, IntSupplier maxSupplier, Function<String, Integer> valueParser, Function<Integer, String> valueGetter, SimpleOption<Boolean> buttonType) implements SimpleOption.IntSliderCallbacks {
    public BothSuppliableIntSliderCallbacks(OrganizablePlayScreensOptions.ScreenRelativeCallbacks screenRelativeCallbacks, SimpleOption<Boolean> buttonType) {
        this(screenRelativeCallbacks.minSupplier, screenRelativeCallbacks.maxSupplier, screenRelativeCallbacks.valueParser, screenRelativeCallbacks.valueGetter, buttonType);
    }

    @Override
    public int minInclusive() {
        return minSupplier.getAsInt();
    }

    @Override
    public int maxInclusive() {
        return maxSupplier.getAsInt();
    }

    @Override
    public Function<SimpleOption<Integer>, ClickableWidget> getButtonCreator(SimpleOption.TooltipFactory<Integer> tooltipFactory, GameOptions gameOptions, int x, int y, int width) {
        if (buttonType.getValue()) {
            return option -> new OptionIntTextFieldWidgetImpl(x + 20, y, width - 20, 20, option, this, tooltipFactory);
        } else {
            return SimpleOption.IntSliderCallbacks.super.getButtonCreator(tooltipFactory, gameOptions, x, y, width);
        }
    }

    @Override
    public Optional<Integer> validate(Integer integer) {
        return Optional.of(MathHelper.clamp(integer, minInclusive(), maxInclusive()));
    }

    @Override
    public Codec<Integer> codec() {
        Function<Integer, DataResult<Integer>> function = value -> {
            int minInclusive = minSupplier.getAsInt() + 1;
            int maxInclusive = maxSupplier.getAsInt() + 1;
            if (value.compareTo(minInclusive) >= 0 && value.compareTo(maxInclusive) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error("Value " + value + " outside of range [" + minInclusive + ":" + maxInclusive + "]", value);
        };
        return Codec.INT.flatXmap(function, function);
    }
}
