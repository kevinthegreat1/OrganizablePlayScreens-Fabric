package com.kevinthegreat.organizableplayscreens.option;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntSupplier;

public record MinSuppliableIntSliderCallbacks(IntSupplier minSupplier, int maxInclusive) implements SimpleOption.IntSliderCallbacks {
    @Override
    public int minInclusive() {
        return minSupplier.getAsInt();
    }

    @Override
    public Optional<Integer> validate(Integer integer) {
        return Optional.of(MathHelper.clamp(integer, minInclusive(), maxInclusive()));
    }

    @Override
    public Codec<Integer> codec() {
        Function<Integer, DataResult<Integer>> function = value -> {
            int minInclusive = minSupplier.getAsInt() + 1;
            if (value.compareTo(minInclusive) >= 0 && value.compareTo(maxInclusive) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error("Value " + value + " outside of range [" + minInclusive + ":" + maxInclusive + "]", value);
        };
        return Codec.INT.flatXmap(function, function);
    }
}
