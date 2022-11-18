package com.kevinthegreat.organizableplayscreens.option;

import net.minecraft.client.option.SimpleOption;
import org.jetbrains.annotations.NotNull;

public class OptionIntTextFieldWidgetImpl extends OptionTextFieldWidgetImpl<Integer> {
    public OptionIntTextFieldWidgetImpl(int x, int y, int width, int height, SimpleOption<Integer> option, BothSuppliableIntSliderCallbacks callbacks, SimpleOption.TooltipFactory<Integer> tooltipFactory) {
        super(x, y, width, height, option, callbacks, tooltipFactory);
    }

    @Override
    public @NotNull String getDisplayValueString() {
        return String.valueOf(((BothSuppliableIntSliderCallbacks) callbacks).displayValueGetter().apply(option.getValue()));
    }

    @Override
    public boolean isValid(String string) {
        if (string.isEmpty()) {
            return true;
        }
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public @NotNull Integer parseValue(String string) {
        return ((BothSuppliableIntSliderCallbacks) callbacks).displayValueParser().apply(string);
    }
}
