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
import java.util.function.UnaryOperator;

/**
 * A {@link SimpleOption.IntSliderCallbacks} implementation with suppliable min and max (both inclusive) bounds,
 * custom value parser and value getter, and the button type to use.
 *
 * <p>This can be used to have dynamic lower and upper bounds for an option.
 * This supports custom display value parser and display value getter
 * used to parse user inputted string and get display value from option value, respectively.
 * This supports two button types: slider and text field, both with the same functionality.
 *
 * @param minSupplier        supplier for the minimum value of the option
 * @param maxSupplier        supplier for the maximum value of the option
 * @param displayValueParser function to parse option value from display value string
 * @param displayValueGetter function to transform option value to display value
 * @param buttonType         the type of button that will be created by the option.
 *                           If the option value is {@code false}, the button will be a slider, and
 *                           if the option value is {@code true}, the button will be a text field.
 */
public record BothSuppliableIntSliderCallbacks(IntSupplier minSupplier, IntSupplier maxSupplier, Function<String, Integer> displayValueParser, UnaryOperator<Integer> displayValueGetter, SimpleOption<Boolean> buttonType) implements SimpleOption.IntSliderCallbacks {
    public BothSuppliableIntSliderCallbacks(int minInclusive, int maxInclusive, SimpleOption<Boolean> buttonType) {
        this(() -> minInclusive, () -> maxInclusive, Integer::parseInt, UnaryOperator.identity(), buttonType);
    }

    public BothSuppliableIntSliderCallbacks(OrganizablePlayScreensOptions.ScreenRelativeCallbacks screenRelativeCallbacks, SimpleOption<Boolean> buttonType) {
        this(screenRelativeCallbacks.minSupplier, screenRelativeCallbacks.maxSupplier, screenRelativeCallbacks.displayValueParser, screenRelativeCallbacks.displayValueGetter, buttonType);
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
            int minInclusive = minSupplier.getAsInt();
            int maxInclusive = maxSupplier.getAsInt() + 1;
            if (value.compareTo(minInclusive) >= 0 && value.compareTo(maxInclusive) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error("Value " + value + " outside of range [" + minInclusive + ":" + maxInclusive + "]", value);
        };
        return Codec.INT.flatXmap(function, function);
    }
}
